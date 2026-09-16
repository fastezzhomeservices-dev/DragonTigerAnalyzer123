# Dragon Tiger Analyzer — Android

A mobile Dragon/Tiger historical-analysis app with:

- Large analysis dashboard
- Screenshot-style colored D/T/SAME round pattern
- Card buttons for Dragon and Tiger: A, 2–10, J, Q, K
- D/T/SAME result buttons
- Automatic local saving of rounds
- History screen with search and result filter
- 3-result pattern analysis: DDD, DDT, DTD, DTT, TDD, TDT, TTD, TTT
- Number-to-next-number transition analysis
- Next-result historical frequency by current card
- 505 supplied rounds preloaded from the original game data

## Build an APK on GitHub — no Android Studio required

1. Create a new GitHub repository (for example `DragonTigerAnalyzer`).
2. Upload **all files and folders inside this project** to the repository. Make sure `.github/workflows/build-apk.yml` is uploaded too.
3. Open the repository on GitHub and click **Actions**.
4. Select **Build Android APK**.
5. Click **Run workflow** → choose your branch → **Run workflow**.
6. Wait for the green check mark.
7. Open that workflow run and scroll to **Artifacts**.
8. Download **DragonTigerAnalyzer-APK**. Inside it is `DragonTigerAnalyzer.apk`.
9. Transfer the APK to your Android phone and install it. Android may ask you to allow installation from that source.

### Automatic build on every update

The workflow also runs automatically when you push to `main` or `master`. Every successful build creates a downloadable APK artifact.

### Optional: one-click release APK

If you create a Git tag such as `v1.0.0` and push it, `release-apk.yml` builds the APK and attaches it to a GitHub Release automatically.

## Important

The app displays historical frequencies and patterns from the entered rounds. These statistics do not guarantee the next game result.
