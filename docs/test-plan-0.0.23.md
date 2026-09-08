# Manual Test Plan — SDK 0.0.23 candidate

Covers everything new since 0.0.22: cold-start drift fix + per-page aspect ratios
(B-1/B-2), empty-ad guard (B-3), `onAdError` callbacks (B-7/B-9), per-instance API
service (B-5), and page-details caching (B-6). Run before tagging 0.0.23.

## Setup

Use the client-simulation host (`test-host/`) — it consumes the SDK artifact exactly
like a client app, on the client toolchain, and its screen shows the SDK version,
current page, last hotspot, and last error.

```bash
# 1. Publish the local candidate
./gradlew :PR1DigitalAdClassic:publishToMavenLocal -Pversion=0.0.23

# 2. Build + install the host against it
cd test-host
./gradlew :app:assembleDebug -PlocalSdk=0.0.23
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Log commands used below:

```bash
adb logcat -c                                   # clear before each case
adb logcat -s PR1TestHost:I                     # host callbacks (incl. onAdError at W)
adb logcat -s PR1DigitalAd-AOS-SDK:I            # SDK internals (API calls etc.)
```

Screen header in the host app: `SDK v0.0.23 | <status>`, `Last hotspot: …`, and an
`Error: …` line whenever `onAdError` fires.

---

## Test cases

### TC-01 — Happy-path load
**Steps:** Fresh install → launch → wait ~15s. Touch nothing.
**Expected:** Ad renders page 1. Log shows `DigitalAdLibVersion = 0.0.23`, one
`onAdLoaded: totalPages=51`, one `onAdPageChanged: 1/51`. **No** `onAdError` lines,
no `Error:` line on screen.

### TC-02 — Cold-start stability (B-1, the drift fix)
**Steps:** Uninstall first (`adb uninstall com.purered.testhost` — clears the image
cache, which is the drift trigger). Install, launch, then hands off for 90 seconds.
**Expected:** Exactly one `onAdPageChanged: 1/51` for the whole 90s. The page never
changes on its own. (Pre-fix behavior: crept to page 4–5 within ~20s.)

### TC-03 — Swipe navigation + per-page layout (B-2)
**Steps:** Swipe forward 5 pages, back 5 pages.
**Expected:** One `onAdPageChanged` per swipe with the right index/adPageId;
6 → back to 1. Each page's image fills the width with no letterboxing or squashed
layout while neighboring pages load. Pager rests where you leave it.

### TC-04 — Page indicators (block paging)
**Steps:** Observe the dots while swiping through 12+ pages; tap a non-active dot.
**Expected:** Max 10 dots in a single row, never wrapping. Active dot walks
left→right through the band; after page 10 the band swaps to the next block and the
dot restarts at slot 1 (full size even on an edge slot). Dots on a side with more
pages shrink toward the edge. Tapping a dot navigates to that page.

### TC-05 — Ad load failure surfaces to the host (B-9)
**Steps:** `adb shell cmd connectivity airplane-mode enable` → force-stop → launch.
**Expected:** SDK shows its "Something went wrong / Try Again" screen. Host screen
shows `Error: adLoadFailed - …` and logcat has one
`onAdError: AdErrorPayload(type=adLoadFailed, … isRecoverable=true)` including the
underlying cause (e.g. DNS failure). Exactly one — not one per frame.

### TC-06 — Recovery from ad load failure
**Steps:** Continue from TC-05: `adb shell cmd connectivity airplane-mode disable`,
wait ~5s, tap **Try Again**.
**Expected:** Normal load: `onAdLoaded: 51`, page 1 renders. No crash, no repeated
error callback after success.

### TC-07 — Page-scoped errors (B-9)
**Steps:** Load the ad normally. Enable airplane mode. Swipe forward 4–5 pages to
reach pages whose data/images have not loaded yet.
**Expected:** Affected pages show the SDK's inline "Error loading Page" text and/or
shimmer; host receives `onAdError` with `pageDetailsFailed` and/or
`pageImageFailed`, each carrying the `adPageId`. Paging keeps working — page-scoped
errors never lock the pager, and the page indicator must track your swipes and hold
position (verified: 4 swipes onto failing pages -> page 5, stable for 30s untouched).
Expect repeated callbacks for the same page if it recomposes while offline — failures
are deliberately retried, so onAdError is an event stream, not one-per-problem. Disable airplane mode afterwards; revisiting a failed
page retries (failures are not cached).

### TC-08 — Page-details caching (B-6)
**Steps:** Fresh launch, wait for load. `adb logcat -c`. Swipe forward 3, back 3.
Then: `adb logcat -d -s PR1DigitalAd-AOS-SDK:I | grep -oE "\{adPageId: [a-f0-9-]+\} getPageDetails Api triggered" | sort | uniq -c`
**Expected:** Every listed adPageId has count **1**. Revisited pages produce no new
"Api triggered" lines (served from cache) but still render with hotspot data.

### TC-09 — Cache freshness (B-6)
**Steps:** After TC-08: force-stop, clear logcat, relaunch, wait for load.
**Expected:** "getPageDetails Api triggered" lines appear again for the visible
page(s) — a fresh ad load never reuses the previous session's page data. Same after
a TC-06 retry: pages refetch.

### TC-10 — Zoom
**Steps:** Pinch to zoom in, pan around, pinch out. (Zoom buttons are disabled in
the host config; enable `ZoomButtonsConfig(enable = true)` to also check the +/−
buttons.)
**Expected:** Zoom 1×–5×, pan clamped to the image, content clipped to the ad box.
Releasing at 1× recenters. No page turn triggered by a pinch itself. (Known low-pri
quirk B-8: panning while zoomed can compete with page swipes.)

### TC-11 — Vertical / classic mode
**Steps:** In `test-host` `MainActivity.kt`, set `adExperience = AdExperience.classic`,
rebuild, launch.
**Expected:** All pages render stacked in one vertical scroll; images load as you
scroll; no crash; `onAdLoaded` fires. Note: `onAdPageChanged` intentionally does not
fire in this mode (deferred, B-4). Revert to `oneAd` afterwards.

### TC-12 — Rotation / config change
**Steps:** Load the ad, swipe to page ~3, rotate the emulator
(`adb shell settings put system user_rotation 1`, then back to `0`).
**Expected:** No crash. Ad re-renders; the ad and page data may refetch (cache is
per loaded ad — that is by design, freshness over reuse). No duplicate error
callbacks, no stuck loading state.

### TC-13 — Minified client build (R8 / consumer rules)
**Steps:** `cd test-host && ./gradlew :app:assembleRelease -PlocalSdk=0.0.23`
**Expected:** BUILD SUCCESSFUL — no missing-class warnings. (Runtime R8 check needs
a signed build; the debug-APK cases above cover runtime behavior.)

### TC-14 — Hotspot tap (limited)
**Steps:** Tap product areas on several pages.
**Expected:** The host's default ad (`8fff1a9e…`/loc `70100005`) has no hotmaps, so
taps do nothing there. Use the hotmapped QA ad instead — swap in adId
`649956ed-3ed4-4d68-b388-aa864a7668e8` / location `01800364` (the one OneAdScreen in
the demo app uses, 6 pages). A tap on a product area shows a loading overlay, then
either dispatches a payload ("Last hotspot: offer id=…") or surfaces
`offerLoadFailed` — never nothing. Verified end-to-end 2026-09-08 via the demo app's
One Ad tab (offer dialog with full payload).

---

## Results

| TC | Result | Notes |
|----|--------|-------|
| TC-01 | | |
| TC-02 | | |
| TC-03 | | |
| TC-04 | | |
| TC-05 | | |
| TC-06 | | |
| TC-07 | | |
| TC-08 | | |
| TC-09 | | |
| TC-10 | | |
| TC-11 | | |
| TC-12 | | |
| TC-13 | | |
| TC-14 | | |

Tester: ____________  Date: ____________  Candidate: 0.0.23 (`-PlocalSdk`)

Automated pre-checks already run during development (for reference): TC-01, TC-02
(75s), TC-03, TC-05, TC-06, TC-08, TC-09, TC-13 — all passing on Pixel_10 emulator.
