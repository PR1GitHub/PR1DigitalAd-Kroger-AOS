# SDK backlog and status

Working list for known issues. Update the Status column as items move.
Statuses: `open` / `in progress` / `fixed <version>` / `wontfix`.

## Functional bugs

| ID | Item | Priority | Status | Notes |
|----|------|----------|--------|-------|
| B-1 | Pager auto-advances several pages on cold start with no input; phantom `onAdPageChanged` events | high | fixed 0.0.23 | Evidence + repro: [pager-auto-advance-investigation.md](pager-auto-advance-investigation.md) |
| B-2 | One shared `aspectRatio` for all horizontal pages; last-measured ratio applied everywhere, churned on every image load | high | fixed 0.0.23 | Suspected root cause of B-1 (`DigitalAd.kt`, `HorizontalDigitalAdView`) |
| B-3 | `% actualPageCount` crashes on an ad with zero pages | high | fixed 0.0.23 | `DigitalAd.kt` horizontal view |
| B-4 | `onAdPageChanged` never fires in vertical (`classic`) mode | lowest (deferred) | open | Not even a parameter of `VerticalDigitalAdView`; needs a "current page from scroll position" definition |
| B-5 | `weeklyAdService` global `var` reassigned inside composition; two `DigitalAd` instances clobber each other (also unkeyed `viewModel()` shares state) | medium | fixed 0.0.23 | Per-instance service via `remember(apiEnv, apiKey)`, keyed ViewModel. Residual: `HotMapViewModel` zoom flag still activity-scoped (cosmetic cross-talk between instances) |
| B-6 | `getPageDetails` refetched per *virtual* page (x1000 looping pager); no cache | medium | fixed 0.0.23 | `AdPageDetailsCache` scoped via `remember(ad)` - dropped on every fresh ad load, failures never cached |
| B-7 | Unchecked `as SuccessResult` cast; image failure silently kills hotmaps for the page | medium | fixed 0.0.23 | `AdPageView.kt` |
| B-8 | Gesture conflicts: backward-swipe block at page 1 too broad; zoomed pan fights pager drag | low | open | |
| B-9 | No error surface for host apps: ad/page/offer failures invisible or SDK-internal only | high | fixed 0.0.23 | `onAdError` callback + `AdErrorType`/`AdErrorPayload`; folds in B-7 |

## Hygiene / operational

| ID | Item | Priority | Status | Notes |
|----|------|----------|--------|-------|
| H-1 | API keys committed; in git history | high | open | Hardcoded QA default removed from SDK source in 0.0.23; keys remain in git history and the demo app - rotation server-side is still the real fix |
| H-2 | Logger: one `POST savelogs` per line, uncancellable scope, payloads in body, `isLoggingEnabled` hardcoded true | medium | open | Batch + expose toggle |
| H-3 | `local.properties` / `.idea/deploymentTargetSelector.xml` tracked despite .gitignore; recurring conflicts | medium | open | `git rm --cached` both (team-wide heads-up needed) |
| H-4 | Dead code: `MapAreaContentView.kt`, commented `ZoomableBoxContent` copy, `HotMaps`/`BoxData`, `parseHtmlString`, `formatEventDates`, `getEventDetails`, `buildAdPageContentDescription` | low | open | |
| H-5 | Model/naming debt: `isHorizontalAd`/`adPagesCount` ignored, `eventPageId` vs `adPageId`, lowercase enum constants, deprecated `forEachGesture`/`getScreenWidth` | low | open | |
| H-6 | `-Xmetadata-version` is an internal compiler flag; supported path = build with an old Kotlin (AGP downgrade discussion) | low | open | Works + verified; revisit if it breaks on a toolchain bump |
| H-7 | README one line; no client integration guide | low | open | |
| H-8 | `onHotSpotClick` never exercised end-to-end: QA ad has zero hotmaps | medium | open | Need an adId/location with mapped offers |

## Client compatibility contract (do not raise without client sign-off)

compileSdk 36 · Kotlin metadata 2.1.0 · kotlin-stdlib 2.1.21 · coil-compose 2.2.2 ·
compose-bom 2025.11.01 · lifecycle 2.10.0 · minSdk 24. Validate every release with
`test-host/` before tagging (`-PlocalSdk=<ver>` for candidates).

## Fixed this engagement

Missing AAR in publication (0.0.19) · empty consumer ProGuard rules (0.0.19) ·
version drift 0.0.12 (0.0.19) · unused deps / missing okhttp (0.0.19) · indicator
wrapping → block paging · compileSdk 37 floor + lifecycle 2.11 (0.0.20) · Kotlin 2.4
metadata + stdlib (0.0.20) · Coil 2.7 crash (0.0.21) · Compose 1.11 release crash (0.0.22)
