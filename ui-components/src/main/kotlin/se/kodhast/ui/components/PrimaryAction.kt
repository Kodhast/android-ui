package se.kodhast.ui.components

import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver

/**
 * Märker vyns primära handling (skill `android:designregler`, D-3): en fylld [AppButton]
 * ([ButtonVariant.Primary] eller [ButtonVariant.Destructive]) och huvudknappen i [AppFloatingToolbar]
 * (FAB:en). `assertSinglePrimaryAction` i `ui-testing` räknar dem – högst en synlig per fönster.
 */
val PrimaryAction = SemanticsPropertyKey<Boolean>("PrimaryAction")

/** Se [PrimaryAction]. */
var SemanticsPropertyReceiver.primaryAction by PrimaryAction

/** De fyllda varianterna – de som drar blicken och räknas som vyns primära handling. */
internal val FILLED = setOf(ButtonVariant.Primary, ButtonVariant.Destructive)
