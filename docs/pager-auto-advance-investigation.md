# Pager auto-advance on cold start (open bug)

**Status:** FIXED (0.0.23) - per-page aspect ratios confirmed the theory. On a fresh
install, 75s untouched produced a single `onAdPageChanged: 1/51` and no drift; swiping
forward/back works and the pager stays put afterwards. Fix: `pageAspectRatios`
(mutableStateMapOf keyed by actual page index) replacing the shared ratio, with an
epsilon compare instead of exact float `!=`, plus an empty-ad guard (B-3).
**Affects:** 0.0.18 and 0.0.19 as shipped (pre-existing; unrelated to the page-indicator rework)
**Found:** 2026-08-31, first runtime session of `test-host/` on the Pixel_10 emulator

## Symptom

On a cold start in horizontal (`oneAd`) mode, the pager advances by itself during the
first ~15-20 seconds while page images are loading, with zero user input. Observed
1 → 2 → 3 → 4, then stable. Each phantom advance fires `onAdPageChanged`.

## Why it matters

- The user's ad visibly opens on page 4, not page 1.
- Clients using `onAdPageChanged` for analytics record page views that never happened.

## Evidence (logcat, test-host, QA ad `8fff1a9e…` loc `70100005`, 51 pages)

```
21:43:33.422  onAdPageChanged: 1/51 adPageId=PG01     <- initial
21:43:46.292  onAdPageChanged: 2/51 adPageId=PG02     <- no input
21:43:49.752  onAdPageChanged: 3/51 adPageId=PG03     <- no input
21:43:50.766  onAdPageChanged: 4/51 adPageId=PG04     <- no input
(stable afterwards; manual swipe at 21:46:03 correctly moved to 5/51)
```

AdPageView compositions (`Entered adPageId(...)`) at 21:43:33.4, 43.9, 44.0, 46.2,
49.8, 51.0 - each phantom advance coincides with the next page composing / an image
finishing its load. Six pages composed in the drift window.

## Working theory

`HorizontalDigitalAdView` keeps ONE shared `aspectRatio` state
(`DigitalAd.kt`, `var aspectRatio by remember { mutableFloatStateOf(0.826f) }`).
Every page's `AdPageView` reports its measured size via `onSizeCalculated`, mutating
the shared value. Each mutation resizes EVERY page (`Modifier.aspectRatio(aspectRatio)`)
and forces a pager-wide relayout while the pager is settling; the pager slides a page
per relayout. The comparison `if (aspectRatio != newRatio)` is an exact float compare,
so near-identical ratios from different pages can also churn it repeatedly.

This shared-ratio design was flagged in the initial code review (issue #6: last
measured page's ratio applied to all pages) - the drift is a second consequence of it.

## Suggested fix direction (unverified)

- Per-page aspect ratio (keyed by actual page index) instead of one shared value, or
  measure once from page 1 and freeze.
- Epsilon comparison instead of `!=` on the float.
- While images load, prefer a stable placeholder ratio over live re-measurement.

## How to reproduce

1. `cd test-host && ./gradlew :app:assembleDebug` (consumes the published JitPack
   artifact - see test-host/settings.gradle.kts header for why).
2. Install on an emulator, launch, touch nothing.
3. Watch `adb logcat -s PR1TestHost:I` - phantom `onAdPageChanged` lines appear
   within ~20s. Cold image cache makes it more reliable (wipe app data between runs).

## Related observation from the same session

The QA test ad (`8fff1a9e-219d-4bee-b6ff-9e9a4c5a231a`, location `70100005`) has NO
hotmaps on any page checked - `getPageDetails` succeeds but `contents` is empty on
all 7 pages fetched ("NO Hot Maps" in SDK logs). `onHotSpotClick` therefore cannot be
exercised with this ad. Runtime verification of the hotspot -> payload path needs an
ad with mapped offers/creatives.
