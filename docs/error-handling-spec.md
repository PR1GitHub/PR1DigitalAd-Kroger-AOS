# DigitalAd SDK — Error Handling Specification

**Audience:** iOS developer replicating the PR1DigitalAd library. This describes the
error-reporting contract shipped in the Android SDK (0.0.23) in platform-neutral
terms, with the exact trigger conditions so both platforms behave identically.
Android reference implementation: `DigitalAd.kt` (types + ad-level errors) and
`AdPageView.kt` (page/offer-level errors).

## Design goals

1. **Additive and optional.** The error callback has a default no-op, so existing
   integrations are untouched. iOS equivalent: an optional closure defaulting to nil.
2. **The host decides the UX.** The SDK reports every failure; the host may show a
   snackbar/toast/banner. The SDK keeps exactly one piece of internal error UI
   (the ad-load retry screen, see `adLoadFailed`).
3. **Nothing fails silently.** Every failure path that previously only logged now
   also reports: page-detail fetches, image dimension fetches, offer fetches, empty
   offers, hotspots with no offer attached, image render failures, empty ads.
4. **Telemetry is not an error.** Failures of the internal `savelogs` logging POST
   are never reported to the host.

## Public API

Android:

```kotlin
enum class AdErrorType {
    adLoadFailed, adEmpty, pageDetailsFailed, pageImageFailed, offerLoadFailed
}

data class AdErrorPayload(
    val type: AdErrorType,
    val message: String,          // human-readable cause, includes underlying error text
    val adPageId: String? = null, // set for page-scoped errors, matches onAdPageChanged ids
    val isRecoverable: Boolean = false // true when the SDK offers its own recovery UI
)

DigitalAd(
    ...,
    onAdError: (payload: AdErrorPayload) -> Unit = {}
)
```

Suggested Swift mirror:

```swift
public enum AdErrorType: String {
    case adLoadFailed, adEmpty, pageDetailsFailed, pageImageFailed, offerLoadFailed
}

public struct AdErrorPayload {
    public let type: AdErrorType
    public let message: String
    public let adPageId: String?
    public let isRecoverable: Bool
}

// DigitalAdView(..., onAdError: ((AdErrorPayload) -> Void)? = nil)
```

Callbacks are delivered on the main/UI thread on both platforms.

## Error catalog

| Type | adPageId | isRecoverable | SDK-internal behavior |
|------|----------|---------------|----------------------|
| `adLoadFailed` | nil | **true** | Shows "Something went wrong" + Try Again button |
| `adEmpty` | nil | false | Renders nothing |
| `pageDetailsFailed` | set | false | Page image may still render; hotspots disabled for that page |
| `pageImageFailed` | set | false | Page shows a warning placeholder (grey circle, yellow warning triangle, "Error loading this page") |
| `offerLoadFailed` | set | false | Loading overlay ends; no `onHotSpotClick` payload dispatched |

### adLoadFailed

- **Trigger:** `GET /api/dacs/{adId}?location={loc}` (getAdDetails) throws — network
  failure, non-2xx, or body deserialization failure.
- **Message:** includes the underlying exception text (e.g. the DNS failure string).
- The SDK renders its own retry screen; a successful retry proceeds to a normal
  `onAdLoaded`. Report `isRecoverable = true` so hosts know retry UI exists.
- Android quirk to be aware of (fine to improve on iOS): the callback fires when the
  error *state* appears or its message changes. A retry that fails with the exact
  same message does not re-fire on Android. Firing once per failure occurrence is
  acceptable and arguably better; do not fire on every UI re-render.

### adEmpty

- **Trigger:** getAdDetails succeeded but the ad's `pages` list is empty.
- Fires once per loaded ad, immediately after `onAdLoaded(0)`. Nothing renders.

### pageDetailsFailed

Two triggers, same type — both mean "this page's hotspots will not work":

1. `GET /api/dacs/{adId}/pages/{pageId}?location={loc}` (getPageDetails) throws.
2. The page-image *intrinsic dimension* fetch fails. Hotspot rectangles from the API
   are in original-image pixel coordinates; the SDK downloads the image at original
   size to learn its dimensions and scale the rectangles to display size. If that
   image request fails, hotspots cannot be placed. (Android history: this path threw
   through an unchecked success-cast and was swallowed — hotspots vanished with no
   trace. Do not replicate that.)

Notes:
- A page whose `contents` list is empty is NOT an error (many pages legitimately
  have no hotspots). Log it, do not report it.
- Individual hotspot JSON entries that fail to parse are skipped and logged, not
  reported; the rest of the page's hotspots still render.
- Fired per page as pages come into view. Android currently refetches page details
  when a page re-enters composition, so the error can repeat for the same page on
  revisit (a page-details cache is planned - backlog B-6; dedupe per page if you
  cache from day one).

### pageImageFailed

- **Trigger:** the on-screen page image request fails (the compressed display URL).
- Reported **once per page appearance** (guarded by a flag) - image libraries can
  invoke error callbacks repeatedly; do not spam the host.
- The SDK shows a warning placeholder for that page (grey circle + yellow rounded
  warning triangle + "Error loading this page", drawn in code - no image assets) and
  keeps paging functional. iOS should render an equivalent placeholder.

### offerLoadFailed

All three trigger conditions end the tap interaction with no `onHotSpotClick`:

1. Tapped hotspot has no offer reference (`offerVersionProductGroupId` missing).
   Message: "Hotspot has no offer attached; nothing to open".
2. `GET /api/dacs/{adId}/offers/{offerId}?location={loc}` returns an empty/null body.
   Message: "Offer details came back empty for this hotspot".
3. Any exception during the click-handling flow (network, parsing).
   Message includes the underlying error text.

Not reported: taps on "creative"/promo hotspots never hit the offer API - the promo
payload is built locally and always dispatches via `onHotSpotClick`.

## Interaction with the success callbacks

- `onAdLoaded(totalPages)` fires on successful ad fetch even when `totalPages == 0`
  (then `adEmpty` follows).
- Page-scoped errors do not stop `onAdPageChanged` - paging keeps working.
- A page can produce BOTH `pageDetailsFailed` and `pageImageFailed` (independent
  requests: hotspot data vs display image).

## Verification (replicate on iOS)

Client-simulation host app renders `lastError` on screen and logs every callback.

1. **Happy path:** normal load -> zero error callbacks.
2. **adLoadFailed + recovery:** enable airplane mode, cold-start -> exactly one
   `adLoadFailed` (DNS cause in message, recoverable) and host shows its message;
   disable airplane mode, tap the SDK's Try Again -> normal `onAdLoaded`, error
   cleared.
3. Offer/page-scoped types need an ad with mapped offers; the shared QA ad
   (`8fff1a9e-…` / loc `70100005`) has no hotmaps, so those paths are verified by
   code review + the same mechanism, not end-to-end (tracked as backlog H-8).

## Explicit non-goals (for parity)

- No error is thrown/raised to the host; everything is callback-based.
- No automatic retry beyond the user-facing ad-load retry button.
- `savelogs` telemetry failures stay internal.
- Vertical/classic mode: same error reporting applies; only `onAdPageChanged` is
  absent there (deferred, backlog B-4).
