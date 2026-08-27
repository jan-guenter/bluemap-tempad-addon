/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.tempad.profile;

import java.util.List;

/** Exact All the Mons 1.2.0 profile `tempad-3.0.4-mc1.21.1`. */
public final class Tempad304Profile {

    public static final String PROFILE_ID = "tempad-3.0.4-mc1.21.1";
    public static final List<ArtifactPin> ARTIFACTS = List.of(
            new ArtifactPin(
                    "tempad",
                    "tempad",
                    "3.0.4",
                    "tempad-1.21.1-3.0.4-all.jar",
                    1_696_464L,
                    "932dd8a1cbb86d7632330ee3b9da43211b7c7a2fb6246443fd8207f74a74eba3"
            )
    );

    private Tempad304Profile() {
    }
}
