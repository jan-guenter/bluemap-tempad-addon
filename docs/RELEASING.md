# Releasing

Prototype work is intentionally light. Before owner acceptance, run only the
focused Java checks, exact candidate verifier, gallery checks, and disposable
staging comparison needed to get useful visual feedback.

After the owner accepts the candidate:

1. Confirm the renderer and bounded gallery contain no scaffold-only markers.
2. Record the accepted integration run and exact candidate JAR in
   `provenance/release.json`, then set the status to
   `owner-accepted-release-candidate`.
3. Build production JAR, sources JAR, POM, and Gradle module metadata with the
   exact promotion Java/Gradle/BlueMap inputs.
4. Require those bytes to match the already sealed `candidate_artifacts`.
5. Run `verifyReleaseCandidate -PreleaseTag=v<version>` with all exact candidate
   JAR Gradle properties.
6. Merge the reviewed commit, create an annotated `v<version>` tag at that
   commit, and let `.github/workflows/release.yml` publish.
7. Compare every downloaded release asset to the locally accepted bytes.
8. Update the private root portfolio, queue, and `workspace.json` in a separate
   orchestration commit.

The release workflow refuses an unpublished migration status. The tag must
exactly equal `v<addon_version>`. No release authorizes production deployment.

The command sequence and required release-provenance fields are recorded in
[`EXECUTION.md`](EXECUTION.md).
