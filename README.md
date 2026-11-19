# android-ui

Gemensamt Compose-bibliotek för Kodhästs Android-appar (ReseApoteket och Dagboken, senare
eventuellt Fonder): ett tema-API och de delade komponenterna, så att samma element ser ut och
beter sig likadant i alla appar och bara finns på ett ställe. Varje app behåller sina egna
färger och typsnitt och skickar in dem i temat; formerna är samma i alla appar.

**Version:** 3.0.0 · Material 3 `1.5.0-beta01` (Expressive) · Compose BOM `2026.09.00` ·
Kotlin `2.4.20` · AGP `9.4.1` · compileSdk 37 · minSdk 30.

## Moduler

| Artefakt | Paket | Innehåll |
|---|---|---|
| `se.kodhast.ui:ui-theme` | `se.kodhast.ui.theme` | `AppTheme`, `Spacing`, `AppShapes`, `AppTypography`, `ExtendedColors`, `ProgressDoneColors`, `Tone`, `ToneColors`, `TRACK_HEIGHT` |
| `se.kodhast.ui:ui-common` | `se.kodhast.ui.common` | ramarnas tillstånd utan UI: `UiError`/`ErrorMapper`, `ListUiState`, `DetailUiState`, `EditorState`, `EditorLoader`, `EditorSheet`, `shownAfter`, `WriteFailures`, `screenSharing`, `DateFormat`, `SyncUiState` |
| `se.kodhast.ui:ui-components` | `se.kodhast.ui.components` | komponenterna och ramarna nedan (drar in `ui-theme` och `ui-common`) |
| `se.kodhast.ui:ui-navigation` | `se.kodhast.ui.navigation` | navigationsramen: `TabBackStack`, `AppTransitions`, `AppNavHost`, och komponentgalleriet `ComponentGallery` (drar in `ui-components`, Navigation 3 och kotlinx-serialization) |
| `se.kodhast.ui:ui-testing` | `se.kodhast.ui.testing` | ramarnas kontraktstester, för appens enhetstester (`testImplementation`) |

## Lägga till i en app

Paketen ligger i GitHub Packages, som kräver inloggning även för läsning. I appens
`settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/Kodhast/android-ui")
            credentials {
                username = providers.environmentVariable("GITHUB_ACTOR").orElse(providers.environmentVariable("GPR_USER")).orNull
                password = providers.environmentVariable("GITHUB_TOKEN").orElse(providers.environmentVariable("GPR_TOKEN")).orNull
            }
            content { includeGroup("se.kodhast.ui") }
        }
    }
}
```

och i versionskatalogen/byggfilen `implementation("se.kodhast.ui:ui-components:3.0.0")` och
`testImplementation("se.kodhast.ui:ui-testing:3.0.0")`.

- **CI i appen:** `GITHUB_TOKEN` räcker när paketet gett apprepot läsåtkomst
  (paketets inställningar → *Manage Actions access*) och jobbet har `permissions: packages: read`.
- **Byta från 0.x:** fram till 0.10.0 hette gruppen och paketen `se.partee71.ui` och adressen
  `ParTee71/android-ui`. De versionerna går fortfarande att hämta från den nya adressen under det
  gamla gruppnamnet. För 1.0.0 byter appen adress, `includeGroup`, gruppen i versionskatalogen och
  importerna (`se.partee71.ui.` → `se.kodhast.ui.`). Inget annat ändrades.
- **Lokalt och i molnsessioner:** `GPR_USER` (GitHub-användaren) och `GPR_TOKEN` (en token med
  `read:packages`) i miljön. Token checkas aldrig in.

## Tema-API

```kotlin
@Composable
fun AppTheme(
    colorScheme: ColorScheme,
    extendedColors: ExtendedColors,
    typeScale: Typography,                                     // appens typskala med appens typsnitt
    typography: AppTypography = AppTypography.from(typeScale), // de namngivna stilarna
    motionScheme: MotionScheme = MotionScheme.expressive(),
    depth: AppDepth? = null,                                   // djup och tryckkänsla; null = platt
    content: @Composable () -> Unit
)

AppTheme.extendedColors   // card, rowTint, toolbar, onToolbar, tones, accents, online, track, progressDone
AppTheme.shapes           // card, cardSegment(top, bottom), row, pill, field, menu, sheet, smallTile, iconTile, fab, dialog
AppTheme.transparent      // ingen fyllnad (t.ex. ovalda segment)
AppTheme.typography       // screenTitle, bigNumber, headline, sectionTitle, quantity, itemTitle, itemSubtitle, body, pill, caption, button
AppTheme.tone(tone)       // ToneColors(container, content)
AppTheme.depth            // AppDepth?: highlight, sheen, shadow, cardEdge, cardShadow, pressedScale, cardSheen, headerShadow
Spacing.xs … Spacing.xxl  // 4, 8, 12, 16, 24, 32 dp
IconSize.marker … .standard // 14 (marker), 16 (pill), 18 (small), 20 (button), 22 (row), 24 (standard) dp
TRACK_HEIGHT, PROGRESS_STROKE // spårets höjd 8 dp, snurrans linje 2,5 dp
```

- **Toner:** `Tone` är en nyckel. Biblioteket känner bara `Tone.Primary`; appen lägger till sina
  som tillägg, t.ex. `val Tone.Companion.Green get() = Tone("green")`, och ger dem färger i
  `ExtendedColors.tones`. En ton utan färger får primärtonen.
- **Former:** samma i alla appar (Mats beslut 2026-11-18: bara färger och typsnitt skiljer) – kort 22 dp,
  rader 18 dp, fält 14 dp, menyer 16 dp, arkets överkant och väljardialogerna 28 dp, FAB 16 dp. Formerna är
  ingen appinställning: `AppTheme` tar inga former (3.0.0).
- **Ikonstorlekar:** `IconSize` som `Spacing` – samma i alla appar; inget eget dp-värde för en ikon.
- **Accentfärger:** `ExtendedColors.accents` är appens dekorativa färgserie (personfärger,
  valbara färger) – används av `Confetti`.
- **Ansluten:** `ExtendedColors.online` är pluppen på `AccountAvatar(online = true)`; standard `null` =
  `colorScheme.primary` (Dagboken sätter sin gröna).
- **Framstegsraden:** `ExtendedColors.track` är spåret i `AppProgress` (standard `null` =
  `colorScheme.primaryContainer`) och `ExtendedColors.progressDone` fyllnaden när allt är klart,
  `ProgressDoneColors(fill, outline)` (standard `null` = `colorScheme.primary` utan kontur; Dagboken sätter
  solgult med mörk kontur, ReseApoteket grönt).
- **Djup (#35):** med `depth = AppDepth(…)` får knappar en ljuskant upptill, ett svagt skimmer och
  en tonad skugga, och sjunker in (krymper till `pressedScale`, skuggan tonar bort) när de trycks.
  Valda chips, segment och verktygsradens flik får ljuskanten och skimret, tonade ikonknappar en
  liten skugga; ovalda chips, segment, flikar, kalenderns dagar, cellerna i `ChoiceGrid` och klickbara
  `InfoPill`/`StatPill` krymper bara nedtryckta. Kalenderns valda dag lyfts som ett valt chip; huvudknappen
  i `AppFloatingToolbar`, FAB-menyns val och dess kryss och `AddSplitButton` med meny får de fyllda knapparnas
  djup (gemensamt i `depthSurface`), och kort en mjuk skugga och en hårfin kant (`cardEdge`; inte på tonade kort eller kort
  med egen kontur). Med `cardSheen` får samma kort ett svagt skimmer som tonar ut över de översta 64 dp
  (i en lista av bitar bara den översta; standard `null` = inget). Bitar av ett kort (`CardSegment`) ritas som ett kort utan skarvar. Den ifyllda
  bocken i `MorphingCheck`/`CheckRow` får ljuskant och en liten skugga och sjunker in medan raden trycks; `QuantityStepper` får ett sänkt
  spår och upphöjda knappar som krymper nedtryckta; `SwitchControl`/`SwitchRow` ett sänkt spår, som
  påslaget får skimmer och ljuskant, och en knopp med knapparnas skugga som sjunker in nedtryckt; det valda i `ChoiceGrid` med ring (`ColorSwatchPicker`,
  `EmojiPicker`) en liten skugga och utan ring skimmer; `AppSlider` ett sänkt spår och en knopp med knapparnas
  skugga, som sjunker in medan den trycks eller dras. Fältet i fokus (`AppTextField` och fälten som bygger på det) får en
  mjuk ring i primärfärgen utanför kanten. Meddelandet i `AppSnackbarHost` flyter med skugga och
  hårfin kant; `AppMenuPopup`, `ConfirmDialog`, `TimePickerPopup` och datumväljaren i `DateField` får den
  hårfina kanten, och `AppBottomSheet` samma kant längs överkanten. Listrader,
  pills, rubriker och tomma lägen är platta med avsikt. Färgerna ger appen per tema; utan `depth` är
  allt som förut.
- Appens egna färger och det som hör till appens domän (t.ex. Dagbokens energiskala, sömnstadier och
  känslotoner) stannar i appen.

Appens tema blir en tunn omslutning:

```kotlin
@Composable
fun ReseApoteketTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) =
    AppTheme(
        colorScheme = if (darkTheme) AppColors.dark else AppColors.light,
        extendedColors = if (darkTheme) AppColors.darkExtended else AppColors.lightExtended,
        typeScale = AppTypeScale,
        content = content
    )
```

## ui-common

Ren tillståndslogik för listor, detaljer och formulär – ingen Firestore, Hilt eller appkod.
Testas med JUnit och Turbine.

- **Fel:** `UiError(cause, message, retryable)` är det enda fel-API:t i ramarna. Appen skriver en
  `ErrorMapper` en gång (`map(Throwable)` från `DataError`, `notFound()`). `UiError` jämförs på
  identitet: samma fel två gånger i rad visas två gånger.
- **Listor:** `ListUiState` (Loading, Empty, Content, Error), `asListUiState(errors[, retry])`,
  `ListLoader`, `ListEvent.Retry`; detaljer: `DetailUiState`, `asDetailUiState`, `DetailLoader`.
- **Formulär:** `EditorState` (validering, ändrat-läge, `saveUnchanged`, läsfel, spara/kör) och
  `EditorLoader` (läsning, "Försök igen", `project`, `prepare`, `afterLoad`).
- **Formulär i ett ark:** `EditorSheet(scope, errors)` med `open`, `update`, `save` och `close`, och arket som visas
  (`EditorSheetState`: `editor`, `loaded`, `context`, `error`, `unsaved`, `closing` (klart-läget "Sparat"), `hiding`
  efter `SAVED_CONFIRMATION_MILLIS`, `canDismiss()`). Ihop med `AppBottomSheet(dirty, canDismiss, hide, error)` och
  `Modifier.blockInput(closing)`. Flyttad från Dagboken (#57).
- **`Flow<Boolean>.shownAfter(delay)`** – visar en indikator först efter en fördröjning.
- **Skrivfel utan ram:** `WriteFailures(scope, errors)` – en skrivning som misslyckas blir ett `UiError` i `failure`
  tills skärmen visat det (`clear()`); `write { … }` kör och rapporterar.
- **Delade flöden:** `screenSharing(forgetWhenStopped = true)` – `SharingStarted` som stoppar efter
  `STOP_TIMEOUT_MILLIS` utan lyssnare och (som standard) glömmer det senaste värdet.
- **`DateFormat`** – det enda sättet att visa datum och tider: `display` ("lör 19 dec 2026"), `short`, `weekdayDay`,
  `month`, `weekdayShort`/`weekdayLong`, `dayAndMonth`, `isoWeek`, `time` ("07:00"), `duration` ("1 tim 30 min"),
  `durationSeconds` ("2 min 15 s") och datumväljarens millisekunder.
  `DateField` och `TimeField` använder den.
- **Inloggningen:** `AuthGate` (Loading, SignedOut, NeedsAccount, UpdateRequired, Ready) – vad `AuthGateHost` visar;
  appens `AuthViewModel` räknar fram det.

## Ramarna

Varje listskärm, redigeringsskärm och detaljskärm i apparna byggs på en av tre ramar, så att laddning,
tomt läge, fel, "Försök igen", "Släng ändringar?", sparfel och arkivering beter sig likadant överallt.

```kotlin
EntityListScreen(title, state: ListUiState<T>, empty: EmptyContent, add: AddAction?, key, …,
    onRetry, group: ((T) -> ListGroup)?, archive: ListArchive?, subtitle, onBack, actions, header,
    subgroup, filter, topBarSize = TopBarSize.Large, footer, refreshing = false, onRefresh = null,
    errorTitle = null, groups = emptyList(), menu = emptyList(), scrollTo = null, onScrolled = {}, row)
EntityEditScreen(title, state: EditorUiState<*>, events: EditorEvents, onSave, onClose, …,
    saveLabel, onArchive, onRestore, delete: DeleteAction?, onRetry, onSaved = onClose, formError,
    horizontalMargin = LocalEditScreenMargin.current, content)
EntityDetailScreen(state: DetailUiState<T>, header: ((T) -> DetailHeader)?, onBack: (() -> Unit)?, …,
    title, onEdit, menu, onRetry, error: UiError?, onErrorShown, subtitle, actions, snackbar,
    sectionGap = LocalDetailSectionGap.current, leading, refreshing = false, onRefresh = null, content)
```

| Skillnad mellan apparna | Parameter | ReseApoteket | Dagboken |
|---|---|---|---|
| Lägg till-knapp | `add: AddAction(label, onClick, menu)`; `null` = ingen knapp, varken i listan eller i tomt läge | `AddAction(…)` (förr `onAdd` + `EmptyContent.actionLabel` + `addMenu`) | `AddAction` eller `null` |
| Kolumner | `ListGroup.columns` är ett tak; så många kolumner om minst 260 dp som får plats används | som förut | nytt: en kolumn på telefon |
| Filter, sidfot, postkort, räknare | `filter`, `footer`, `ListGroup.cards`, `ListGroup.showCount`, `topBarSize` | – | som förut |
| Rulla till en rad | `scrollTo: ListScrollRequest(key, message)` – till gruppens rubrik när raden är först i gruppen, väntar tills raden finns; `onScrolled` släpper begäran; `message` visas som snackbar (t.ex. "Inga poster den …") | – | Välj dag i Dagbok (#68) |
| Arkiveringsfel | `ListArchive.error: UiError` | som förut | förr `failure: Failure` |
| Formulärets sidomarginal | `horizontalMargin`, standard `LocalEditScreenMargin` (sätts en gång i appens tema) | `Spacing.xl` (standard) | `Spacing.l` |
| Formulärfel överst | `formError` | – | som förut |
| Tangentbordet | `imePadding` före `verticalScroll` (fältet hamnar ovanför tangentbordet) | nytt | som förut |
| Detaljens flikläge | `onBack = null`: stor topprad som fälls ihop, `title`, `subtitle`, `actions`, `snackbar`, plats för verktygsraden | – | som förut |
| Detaljens avstånd mellan sektioner | `sectionGap`, standard `LocalDetailSectionGap` (sätts en gång i appens tema) | `Spacing.m` (standard) | `Spacing.l` |
| Hopfällbar grupp | rubriken minst 48 dp, läses som knapp ("Fäll ut"/"Fäll ihop"), chevronen fjädrar | nytt | som förut |
| Knappar i tomt läge utan lägg till | `EmptyContent.actions: List<EmptyAction>` (standard inga); står under lägg till-knappen, eller ensamma med `add = null` | – | – (Fonder: flikarna med plusmeny, "Ny transaktion" + "Importera avräkningsnota") |
| Läsfelets rubrik | `errorTitle` (standard `null` = "Kunde inte hämta listan") | – | – |
| Kodhäst-sidhuvudet | Flikläget (lista utan `onBack` med `TopBarSize.Large`, detalj med `onBack = null`) ritar `AppHeader` när appen satt `LocalAppIdentity`; utan den dagens stora topprad | – | – (först Dagboken #435) |
| Knapp i gruppens rubrik | `ListGroup.action: GroupAction(icon, label, onClick)` – tonad rund knapp mellan räknaren och pilen, tryckbar också hopfälld (standard `null`) | packlistans plus per sektion (#172) | – |
| Hopfällt läge utifrån | `ListGroup.fold: GroupFold(expanded, onToggle)` – appen håller läget (t.ex. under appsessionen); `collapsible = true` utan `fold` som förut (listan håller läget, börjar hopfälld) | packlistan | som förut |
| Grupper som alltid visas | `groups: List<ListGroup>` – först, i ordning, också utan rader (`ListGroup.emptyText`); en tom lista visar grupperna med `empty` som kort överst (standard inga = som förut) | packlistan | – |
| ⋮-meny | `menu: List<AppMenuItem>`; "Visa arkiverade" läggs sist i samma meny, med egen text och ikon via `ListArchive.showLabel`/`hideLabel`/`toggleIcon` | Räkna om, Dölj packade, Visa dolda (N)/Göm dolda | – |
| Dra för att uppdatera | `onRefresh` (standard `null` = ingen gest) och `refreshing` i listan och detaljen; Expressive-indikatorn överst, ytan har taggen `ENTITY_REFRESH_TAG`; samma yta finns fristående som `AppPullToRefresh` | – | – (Fonder: Fondsök, Portfölj, Fonddetalj; Översikt, Innehav och Beslut med `AppPullToRefresh`) |

- **Arkivering:** `ArchiveActions(scope, errors, setArchived, undoFormat, errorMessage, showing)` är logiken i
  listans ViewModel (svep, Ångra, "Visa arkiverade", `offerUndo` för annat än arkivering);
  `visible(source) { it.archived }` filtrerar och `collectAsListArchive()` ger listans `ListArchive`.
  Raderna sveps med `SwipeToHide`; `UndoSnackbar` visas av listramen. `showing` är läget för "Visa
  arkiverade" – ett eget `MutableStateFlow` låter läget stå kvar längre än ViewModeln.
- **Lägg till-knappens tagg:** `ENTITY_LIST_ADD_TAG`, som kontraktet använder för att pröva att knappen saknas.

## Sidhuvudet (Kodhäst)

`AppHeader` (#42) är det gemensamma sidhuvudet på apparnas flikar, och det som gör en app till en
Kodhäst-app. Det är byggt för **varje** app, också kommande: inget i det är Dagbokens. Ikonen, namnet,
färgerna, typsnitten och kontot kommer från appen; en app som inte sätter något ser ut som förut.

- **Vila (högst upp), 144 dp:** appikonen (40 dp) och appnamnet överst, kontot längst till höger; under dem
  fliknamnet stort (`screenTitle`) och undertiteln. Sidans färg, ingen skugga.
- **Rullat, 64 dp:** ikonen (32 dp), fliknamnet (`sectionTitle`) bredvid den och kontot. Kortens färg och en
  skugga (`AppDepth.headerShadow`, standard `cardShadow`; utan djup en hårfin linje i `outlineVariant`)
  när innehåll ligger under. Rak nederkant.
- **Däremellan** krymper raden med fingret och samma rubrik flyttas upp till ikonen; raden fälls ut igen
  först högst upp (`exitUntilCollapsedScrollBehavior`). Rubriken är alltid flikens namn – det appen vill
  säga mer (en hälsning, ett datum) står i undertiteln.

**Så slår en app på det** – en gång, runt appens innehåll (temat eller navigationen):

```kotlin
CompositionLocalProvider(
    LocalAppIdentity provides AppIdentity(R.drawable.ic_app_header, stringResource(R.string.app_name)),
    LocalAppBrandStyle provides AppBrandStyle(headerDots = true),          // Kodhästs prickar före rubriken
    LocalTopBarSyncing provides syncing,                                   // prickarna hoppar medan ändringar väntar
    // Sidhuvudet syns bara inloggad; utloggad visar AuthGateHost inloggningen (se Inloggningen).
    LocalTopBarAccount provides { AccountAvatar(user.name, onClick = openAccount, online = online, photoUrl = user.photoUrl) }
) { content() }
```

1. **Ikonen** är en vektor eller bitmapp i appens `res/drawable` – inte den adaptiva startikonen.
2. **Färgerna:** `ExtendedColors(online = …)` om pluppen inte ska vara `primary`;
   `AppDepth(headerShadow = …)` om sidhuvudets skugga ska skilja sig från kortens.
3. **Kontot:** inloggningen och vad trycket öppnar är appens. Fotot är `photoUrl` – Coil laddar det och sparar
   det i appens privata cache, så att det syns utan nät; appen anropar `CoilProfilePhotoCache(context).clear()`
   vid utloggning (`se.kodhast.ui.account.ProfilePhotoCache`) och loggar aldrig adressen. Anslutningsläget
   (`online`: `true` plupp, `false` ingen, `null` inget läge) kommer från `ConnectivityMonitor(context, scope)`
   (`NetworkStatus.online`: validerat internet via systemets standardnätverk), en instans per app – i Hilt en
   `@Singleton`-provider. Biblioteket drar in Coil 3 och behörigheterna `INTERNET` och `ACCESS_NETWORK_STATE`.
4. **Skärmarna:** flikar på ramarna får sidhuvudet av sig själva. En flik utan ram anropar
   `AppHeader(title, scrollBehavior)` med `TopAppBarDefaults.exitUntilCollapsedScrollBehavior()` och
   `Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)` på skärmen. Underskärmar behåller
   `AppTopBar(size = TopBarSize.Small)`.
5. **Synken:** med `headerDots` visas den av prickarna (TalkBack: "Ändringar väntar på servern") – i sidhuvudet
   och, från 3.1.0, före rubriken i underskärmarnas `AppTopBar`. Det är appens enda indikator för
   bakgrundsarbete; en app som behåller en egen indikator sätter den som `LocalTopBarStatus` som förut.
6. **Tester:** appens kontraktstester körs med appens tema – sätts identiteten där, prövas ramarna med
   sidhuvudet. Skärmdumparna tas om i appens PR.

## Sidfoten (Kodhäst)

Flikraden (`AppFloatingToolbar`) och plusknappens meny (#44) följer samma Kodhäst-standard. Knapparna är
desamma i alla appar – vald flik som kapsel i `primary` med ikon och namn, övriga bara ikon, huvudknappen i
`secondary` till höger – och appen väljer bakgrund och meny en gång i `AppBrandStyle`:

```kotlin
LocalAppBrandStyle provides AppBrandStyle(
    headerDots = true,
    toolbarStyle = ToolbarStyle.Tonal,     // flikraden i appens ljusa ton (primaryContainer)
    actionMenu = ActionMenuStyle.Fab,      // plusknappens val växer upp ur knappen
    actionColor = ActionColor.Secondary    // plusknappens färg; Tertiary när secondary är neutral (ReseApoteket)
)
```

- `ToolbarStyle`: `Toolbar` (standard, `extendedColors.toolbar`), `Surface` (kortens yta), `Tonal` (Kodhäst-standard:
  `primaryContainer`/`onPrimaryContainer`, Material 3 Expressives "vibrant") och `Filled` (`primary`).
- `ActionMenuStyle`: `Popup` (standard, rullgardin) och `Fab` (Kodhäst-standard: valen som kapslar i `primary` med
  ikon från `AppMenuItem.icon`, knappen blir ett kryss "Stäng", ljus slöja över innehållet; tryck utanför, bakåt
  eller ett val stänger).
- Plusknappens meny är `ToolbarAction(label, icon, menu = listOf(AppMenuItem(…)))`; ett eget ark för valen behövs inte.
- Utan inställning ser raden ut som förut.

## Inloggningen

`AuthGateHost` är appens rot och visar det `AuthGate` säger; inloggningen själv (Credential Manager,
Firebase, hushåll eller användare) stannar i appens `AuthViewModel` – biblioteket har ingen Firebase.

```kotlin
AuthGateHost(
    gate = state.gate,
    signIn = {
        SignInScreen(
            appIcon = R.drawable.ic_luggage,                         // appens ikon, inte Kodhästs häst
            appName = stringResource(R.string.app_name),
            welcome = stringResource(R.string.sign_in_welcome),
            note = stringResource(R.string.sign_in_note),
            busy = state.busy,
            error = remember(state.error) { state.error?.let(AppErrors::map) },
            onErrorShown = { onEvent(AuthEvent.ErrorShown) },
            onSignIn = { onEvent(AuthEvent.SignIn(context)) }
        )
    },
    updateRequired = {
        UpdateRequiredScreen(
            message = stringResource(R.string.update_required_message),
            note = stringResource(R.string.update_required_note),
            releasesUrl = stringResource(R.string.releases_url)
        )
    }
) { AppNavigation() }
```

"Logga in med Google", "Loggar in …", "Uppdatera appen", "Hämta senaste versionen" och ikonerna `ic_login`
och `ic_download` finns i biblioteket; appen kan ta bort sina kopior. Appens eget `AuthGate`-enum ersätts av
bibliotekets (`NeedsHousehold`/`NeedsUser` heter `NeedsAccount`).

## Inställningarna

Delarna som båda apparnas inställningar har; vilka rader och kort inställningarna har stannar i appen.
`SettingsPage` är sidan (topprad, rullning, marginal, luft för flikraden) och `SettingsCard` ett kort som visar ett
fel från en åtgärd i just det kortet sist i sig.

```kotlin
SettingsPage(stringResource(R.string.tab_settings), onBack = onBack) {
    GroupLabel(stringResource(R.string.settings_account))
    SettingsCard(error = state.error?.takeIf { it.section == Account }?.let { stringResource(it.error.toMessage()) }) {
        AccountRow(user.name, user.email, user.photoUrl)
        AppDivider()
        SignOutRow(stringResource(R.string.app_name), onSignOut = { onEvent(SettingsEvent.SignOut) })
    }
    …
}
val access = rememberNotificationAccess()                       // läses om varje gång skärmen visas
if (!access.allowed) {
    NoticeBanner(stringResource(R.string.notifications_off), R.drawable.ic_bell, onClick = access.openSettings,
        detail = stringResource(R.string.notifications_off_note))
}
AboutCard(stringResource(R.string.app_name), BuildConfig.VERSION_NAME) {
    AppDivider()
    ItemRow(stringResource(R.string.settings_medicines), subtitle = …)  // appens egna rader
}
```

- `NotificationAccess(allowed, ask, openSettings)`: `ask` frågar om behörigheten (Android 13+), `openSettings` öppnar
  appens notisinställningar. `Context.canPostNotifications()` är samma kontroll utanför Compose (t.ex. innan en notis visas).
  Dagbokens exakta larm och helskärm stannar i Dagboken.
- "Logga ut", "Logga ut?", "Inloggad med Google", "Version %s", "Öppna" och ikonen `ic_logout` finns i biblioteket;
  appen kan ta bort sina kopior (också `sign_out_message`, som biblioteket inte läser).

## Navigationsramen (ui-navigation)

Flikarna med egna back stackar, skärmbytena och skalet runt `NavDisplay` – samma i alla appar.
Appen behåller sina nycklar (`@Serializable` och `NavKey`), vilka skärmar som finns och plusmenyns val.

```kotlin
@Serializable sealed interface AppKey : NavKey
@Serializable sealed interface TopLevelKey : AppKey
// … flikarnas och skärmarnas nycklar
val TOP_LEVEL: List<AppKey> = listOf(TripsKey, PersonsKey, FamilyItemsKey)

val backStack = rememberTabBackStack(TOP_LEVEL, AppKey.serializer())
AppNavHost(
    backStack,
    tabs = listOf(ToolbarItem(stringResource(R.string.tab_trips), R.drawable.ic_luggage), …),
    entryProvider = appEntries(backStack),
    sync = syncState,                      // SyncUiState ur appens SyncViewModel
    onSyncErrorShown = { sync.onEvent(SyncEvent.WriteErrorShown) },
    action = ToolbarAction(addLabel, R.drawable.ic_add, addMenu),
    onPopped = { key -> … },
    account = { AccountAvatar(…) }
)
```

- `TabBackStack`: appen startar på första fliken; varje flik minns sin undersida; tillbaka i en annan fliks rot går
  till första fliken, i dess rot stänger systemet appen. `select`, `push` (ett dubbeltryck öppnar en gång),
  `pop(): Boolean`, `popIfTop(key): Boolean`, `closer(key, then)` (skärmens `onClose`), `openInTab(tab, key)`
  (notiser och djuplänkar: överst i fliken, det som var öppet ligger kvar under). Läget sparas som JSON i
  samma format som apparnas tidigare `AppBackStack`; ett läge som inte går att läsa eller inte passar flikarna
  ger startläget.
- `AppNavHost`: verktygsraden bara i en fliks rot, `LocalBottomClearance`, ViewModel per skärm, flikrötternas
  sparade tillstånd över flikbyten, synkprickarna (`LocalTopBarSyncing`), kontot (`LocalTopBarAccount`) och en
  nekad skrivning som meddelande ("En ändring kunde inte sparas och har ångrats. …", strängen
  `sync_write_rejected`).
- `AppTransitions.forward()`/`back()`: skärmbytet – glider en fjärdedel och tonas med fjädring; tillbaka
  baklänges. Används av `AppNavHost`; en egen `NavDisplay` skriver `transitionSpec = { AppTransitions.forward() }`.

## Komponentgalleriet

`ComponentGallery` (i `ui-navigation`, paketet `se.kodhast.ui.navigation.gallery`) visar varje publik
komponent i `ui-components` och `ui-navigation` på en skärm, i appens tema och med påhittad data –
ramarna i fast höjd, dialoger, ark och meddelanden bakom knappar. Bibliotekets sektioner kommer först,
appens egna (det som hör till appens domän) efter. Appen visar galleriet bara i debug-bygget:

```kotlin
ComponentGallery(
    onBack = onBack,
    appSections = listOf(
        GallerySection("Personer och dosering") { PersonRows() },
        GallerySection("Diagram") { Diagrams() }
    )
)
```

Bibliotekets `UiConsistencyTest` kräver att varje publik komponent (utom en `@Deprecated`) står i
`LibraryGallery.kt`; en ny komponent läggs in där i samma PR. Apparnas test kräver bara appens egna
komponenter i `appSections`. Texterna `gallery_title`, `gallery_error_offline`, `gallery_error_not_found`
och `gallery_name_required` kan ersättas av appen.

## ui-testing – kontraktstesterna

En skärm som använder en ram kör ramens kontrakt i sitt Robolectric-test och testar sedan bara det
som är unikt för skärmen. Kontrakten prövar ramens beteende genom appens skärm, med appens tema och fel:

| Kontrakt | Prövar |
|---|---|
| `runListScreenContract(environment, item, itemText, emptyTitle, addLabel: String?, emptyActions = emptyList(), errorTitle, screen)` | laddar; tomt läge med lägg till-knapp (eller ingen knapp med `addLabel = null`) och skärmens egna knappar (`emptyActions`); fel med rubriken (`errorTitle`) med felets text och "Försök igen"; innehåll med lägg till ovanför listan |
| `runEditScreenContract(environment, editor, makeInvalid?, makeValid, invalidMessage?, saveLabel, screen)` | "Spara" aktiv först när giltigt och ändrat; fältfel efter ändring; "Släng ändringar?" vid bakåtpilen och systemets bakåt; sparfel som snackbar (kvitterat); lyckad sparning stänger (kvitterat); bakåt utan ändringar stänger direkt |
| `runDetailScreenContract(environment, value, valueText, editable = true, tab = false, screen)` | laddar utan Redigera; läsfel med "Försök igen"; något som inte finns utan "Försök igen"; innehåll med Redigera och tillbakapil (ingen pil i flikläge) |

Appen skriver sin miljö en gång i testkoden:

```kotlin
val contracts = ContractEnvironment(AppErrors, DataError.Offline) { ReseApoteketTheme(content = it) }

@Test
fun `listan uppfyller listkontraktet`() = rule.runListScreenContract(contracts, item, "Solkräm", "Inga familjeartiklar än", "Lägg till") { state, onAdd, onRetry ->
    FamilyItemsContent(state, onAdd = onAdd, onRetry = onRetry)
}
```

Modulens egna tester kör kontrakten mot bibliotekets ramar – och prövar att de fäller en skärm som
inte använder ramen rätt.

## Komponenter

| Komponent | Vad | Skillnad mot apparnas tidigare version |
|---|---|---|
| `AboutCard` | Om-kortet: `appName`, "Version …" (`version`) och `MakerMark` under texten; `extra` är appens egna rader i samma kort | Flyttad från ReseApoteket och Dagboken (#54) |
| `AccountAvatar` | Kontots avatar: `name` (initialer, "Anna Berg" → "AB"; utan namn en person-ikon), `onClick` (`null` = bara en bild utan semantik), `online` (`true` plupp i `ExtendedColors.online` och "Ansluten", `false` "Utan anslutning", `null` inget läge), `photoUrl` (profilfotot ovanpå initialerna, laddat och cachat på disk med Coil), `photo` (egen slot, går före `photoUrl`). 36 dp i 48 dp tryckyta, "Konto och inställningar" | Flyttad från Dagboken (#42); `photoUrl` och cachen flyttade från Dagboken (#46) |
| `AccountRow` | Kontot överst i inställningarna: `AccountAvatar`, `name` och `email` under (utan e-post "Inloggad med Google"; utan namn står e-posten som titel), `photoUrl`; ingen tonad bakgrund | Flyttad från ReseApoteket och Dagboken (#54); Dagbokens tonade rad och extra rad "Inloggad med Google" försvinner (Mats beslut) |
| `AddSplitButton` | "Lägg till ▾" – huvudåtgärd och pil med fler val; `style` (standard `LocalAppButtonStyle`) ger höjd och tonad färg som den sekundära `AppButton` | Menyn är en lucka: `menu = items.asSplitMenu()` (ReseApoteket) i stället för Dagbokens `menuItems` |
| `AppBottomSheet` | Ark med rubrik; innehållet rullar alltid. `error` (`UiError`), `dirty` ("Släng ändringar?" vid bakåt, svep och tryck utanför), `canDismiss`, `hide`, `footer` (knappraden står fast i botten), `subtitle` (en rad under rubriken) | Dagbokens parametrar; ReseApotekets rullning gäller också med `footer`; `subtitle` från ReseApoteket (#75) |
| `AppButton` | Den enda knappen: Primary, Secondary, Outlined, Text, Destructive; `loading`, `compact`, `contentDescription`. Appens knappstil `AppButtonStyle(minHeight, morph, secondaryTone)` sätts en gång med `LocalAppButtonStyle` (eller parametern `style`); `AppButtonDefaults.MinHeight` 56 dp / `CompactHeight` 40 dp | Fonder: `AppButtonStyle(minHeight = 40.dp, morph = true, secondaryTone = Tone.Primary)`; standard bevarar ReseApoteket och Dagboken |
| `AppCalendar` | Månadskalendern: `month`, `onMonthChange`, `marked` (punkt), `selectedDate` (fylld primärfärg i `shapes.row`), `onDateClick`, `today` (punkt i `secondary` med ring i `progressDone.outline`), `dimFuture` (framtiden nedtonad och ovalbar, pilen till nästa månad stängd). TalkBack läser datum, "idag" och "har poster". `DayMarkerDot` och `dayDescription` är samma markering och uppläsning för en datumremsa | Flyttad från Dagbokens `DagbokenCalendar` och `DayMarker` (#68); ser ut som förut, men med `dimFuture` stängs nu också pilen till månaden efter idag |
| `AppCard` | Kortet: `tone`, `contentColor` (standard `null`: ärver omgivande `LocalContentColor`; Dagboken `onSurface`), `onClick` (hela kortet en nod). Appens kortstil `AppCardStyle(border, padding, segmentGap, contentSpacing)` med `LocalAppCardStyle` (eller `style`); ett tonat kort har ingen kontur | ReseApotekets `ApotekCard` heter nu `AppCard`. Fonder: kontur 1 dp `outlineVariant`, `padding = Spacing.l`, `contentSpacing = 0.dp`; Filled/Error är Fonders egna toner |
| `AppDivider` | Avdelare mellan rader i ett kort | – |
| `AppFilterChip` | Filterchip; `icon`, `onLongClick`, `value` (värde efter etiketten), `emphasized` (det aktiva bland flera valda), `pending` (`ChipPending`: väntande läge, tonat med streckad kant, ikon och tilläggstext) | `chipColors()` är publik för egna chips |
| `AppFloatingToolbar` | Toppnivånavigeringen: flytande flikar; `action` (`ToolbarAction`, huvudknapp i `secondary` till höger – Dagboken; med `menu: List<AppMenuItem>` fäller knappen ut en meny och läses "Hopfälld"/"Utfälld" – Fonders plusknapp), `style: ToolbarStyle`, `actionMenu: ActionMenuStyle` och `actionColor: ActionColor` (huvudknappens färg, `Secondary` eller `Tertiary`; standard ur `AppBrandStyle`; se Sidfoten), `ToolbarItem.badgeCount` (märke med antal i `secondary` på en ovald flik, läses "Beslut, 3 nya" – Fonder). `LocalBottomClearance`, `FloatingToolbarClearance`, `NoBottomClearance` | – |
| `AppHeader` | Kodhäst-sidhuvudet på en flik (se Sidhuvudet): `title`, `scrollBehavior`, `subtitle`, `syncing` (`LocalTopBarSyncing`), `identity` (`LocalAppIdentity`), `status`, `account` (`LocalTopBarAccount`), `actions`, `titleModifier`; taggen `APP_HEADER_TAG` | Ny (#42); ramarnas flikläge med `LocalAppIdentity` |
| `AppIconButton` | Ikonknapp, minst 48 dp, alltid `contentDescription`; `tone` (platta och ikon för Tonal, ikonen för Plain; standard Material-färgerna) | – |
| `AppLoading` | Expressive laddningsindikator mitt på ytan; med `AppBrandStyle(dotLoading = true)` Kodhästs två prickar som hoppar | – |
| `AppProgress` | Framstegsraden "x av y": appens text (`label`, `doneLabel`) ovanför ett rundat spår (`ExtendedColors.track`, standard `primaryContainer`) som fylls fjädrande i `primary`; när allt är klart fyllnaden i `ExtendedColors.progressDone` (`ProgressDoneColors(fill, outline)`, standard `primary` utan kontur) och `doneLabel`. `celebrate = false` håller raden i primärfärgen (en tidigare dag), `compact` ger ett kort spår och "9 / 14" för en listrad. TalkBack läser texten och framsteget (`progressBarRangeInfo`); `doneDescription` = egen talad text vid klart (standard `doneLabel`) | Ersätter Dagbokens `ProgressBar` och ReseApotekets `SuitcaseProgress` (#55, form A): Dagboken ser ut och låter som förut med sin `track`, solgula `progressDone` med kontur och `doneDescription`; i ReseApoteket försvinner vågen, det stora talet, resväskerutan och det gröna klart-kortet |
| `AppMenu` | Menyn bakom ⋮ (`AppMenuItem`: `icon`, `destructive`, `section` (rubrik), `checked` (kryssrad som håller menyn öppen), `supporting`/`supportingTone` (undertext), `muted` (dämpad etikett)) | `section`, `checked` från Dagboken; `asSplitMenu()` från ReseApoteket |
| `AppNavHost` | Navigationsramen i `ui-navigation` (se Navigationsramen): `backStack`, `tabs`, `entryProvider`, `sync`, `onSyncErrorShown`, `action`, `onPopped`, `account` | Flyttad från ReseApoteket och Dagboken (#52); appens `onExit` behövs inte – i första flikens rot stänger systemet appen |
| `AppMenuPopup` | Själva menyn, för egna knappar som öppnar en meny | Publik (Dagboken använder den direkt) |
| `AppPullToRefresh` | Dra för att uppdatera runt eget rullbart innehåll, för skärmar utan ram: `refreshing`, `onRefresh` (`null` = ingen gest); samma yta och tagg (`ENTITY_REFRESH_TAG`) som i ramarna | Fonder: Översikt, Innehav, Beslut |
| `AppSegmentedChoice` | Ett val av få alternativ som segmentknappar | – |
| `AppSlider` | Skjutreglaget: `label`, heltalsvärdet (`value`, `valueRange`, `step`), `valueText` och valfri `badge` (t.ex. en nivå-`InfoPill`) ovanför spåret; `trackBrush` (standard `primary`, en skala skickar sin gradient) och `thumbColor` (ringen). Går skalan under noll utgår fyllnaden från nollan, markerad med ett streck. Minst 48 dp högt; TalkBack läser `label` och `stateDescription` (standard `valueText`) | Flyttad från Dagbokens `ValueSlider` (#76): energiskalan, nivåpillen och färgerna är parametrar som Dagboken skickar. Med djup sänkt spår och lyft knopp (#74) |
| `AppSnackbarHost` | Den enda platsen där meddelanden visas | – |
| `AppTextField` | Det enda textfältet; `maxLength` (`AppTextFieldDefaults.SHORT_MAX_LENGTH` 200 / `LONG_MAX_LENGTH` 2000), `maxLines`, `suffix`, `valueIsPlaceholder` | Dagboken anger `maxLength = 5000` för långa fält |
| `AppTopBar` | Skärmrubriken, stor eller liten; tillbakapilen `TopBarBackStyle(variant, tone)` via `LocalTopBarBackStyle` (eller parametern `backStyle`), som också ramarna följer. Med `AppBrandStyle.headerDots` står Kodhästs prickar före rubriken som i `AppHeader` och hoppar medan `syncing` (standard `LocalTopBarSyncing`) är sant | Synkindikatorn är appens: den sätts som `LocalTopBarStatus` (eller parametern `status`). `titleModifier` läggs på rubriken (också via `EntityDetailScreen`). Standardpilen är den tonade plattan; Fonder: `TopBarBackStyle(variant = IconButtonVariant.Plain)` |
| `AuthGateHost` | Appens rot: `gate` (`AuthGate`) visar `AppLoading`, `signIn` (utloggad och `NeedsAccount`), `updateRequired` eller `content`; sätter appens bakgrund en gång | Flyttad från apparnas `AppRootContent` (#53) |
| `BrandMark` | Kodhästs märke: hästen (`horseColor`, standard `onSurfaceVariant`) och de två prickarna (`dotsColor`, standard `primary`), dekor utan semantik; storlek med `modifier`, standard `BrandMarkDefaults.Size` 16 dp (profilens minsta) | Ny (android-ui #28) |
| `CardSegment` | Ett segment i ett sammanfogat kort (`top`, `bottom`), `contentColor`, `onClick`, `style`; med kontur delar bitarna skiljelinje | Ersätter Fonders `AppCardGroupPosition` |
| `CheckRow` | Kryssrad med morfande kryss; `onClick` (krysset blir egen knapp), `accent`, `note`, `menu` (långtryck + ⋮, går före bubblan), `below`, `enabled`, `done`/`tinted` (standard `checked`: överstruken och tonad) | Dagbokens `note`, `menu`, `below`, `enabled`; Fonder `done = false, tinted = false` för "ta med" |
| `CollapsibleHeader` | Titelraden i det som fälls ihop: hela raden växlar, minst 48 dp, läses som knapp ("Fäll ut"/"Fäll ihop") med läget som `stateDescription`, chevronen sist fjädrar ett halvt varv. `expandLabels(expanded)` (`ExpandLabels`: `action`, `state`) och `chevronRotation(expanded)` för egna chevronknappar | Var intern (hopfällbara grupper i listramen); ersätter Dagbokens `CollapsibleHeader`, `expandLabels` och `chevronRotation` |
| `ChipRow` | En rad chips som bryts eller rullar | – |
| `ChoiceChips` | Ett val av flera som chips; `onClear` (Dagboken); `more` (resten under ett fält `moreLabel`, standard "Fler", som öppnar en meny – favoriter som chips). Överlagring för flerval (`selected: Set<T>`, `onToggle`) | Flervalet är nytt (Dagboken #321); `more` ersätter Dagbokens `TypeChoiceField` (#76) |
| `ChoiceGrid` | Val i ett rutnät med egen cell; `selectionRing` (Dagboken: ring i primärfärg) | – |
| `ColorSwatchPicker` | Färgval; `marker` krävs: `SwatchMarker.RingAndCheck(checkColor)` (ReseApoteket) eller `GridRing` (Dagboken) | `marker` saknar standard – bockens färg är appens |
| `ComponentGallery` | Komponentgalleriet i `ui-navigation` (se Komponentgalleriet): `onBack`, `appSections` (`GallerySection(title, content)`, appens egna sektioner efter bibliotekets) | Ersätter apparnas skal och deras sektioner för bibliotekets komponenter (#56) |
| `Confetti` | Kort konfettiregn som belöning | `colors` (standard `AppTheme.extendedColors.accents`) |
| `ConfirmDialog` | Bekräftelse; `destructive`, `onDecline`, `dismissLabel` (`null` = bara en knapp), `content` (fält under texten) | Dagbokens `dismissLabel: String?` och `content` |
| `DateStrip` | Veckoremsan: veckan som innehåller `week` som sju dagar med veckodag, datum och `DayMarkerDot`; `selectedDate` fylld primärfärg, `datesWithEntries`, `today` (bock när `todayDone`, läses "Klar"), framtiden tonad och ovalbar. Svep byter vecka (`onWeekChange` får måndagen, aldrig en vecka med bara framtida dagar), samma byten som TalkBack-åtgärder | Flyttad från Dagboken (#76) |
| `DateTimeRow` | Datum och tid för en post på en rad: `DateField` och `TimeField` bredvid varandra; `dateLabel`, `timeLabel` (standard "Datum", "Tid") | Flyttad från Dagboken (#57) |
| `DateField` | Datumfält (kotlinx `LocalDate`, "lör 19 dec 2026"); `emptyLabel`, `onClear` (✕ "Till …"), `context` för TalkBack | Dagbokens parametrar |
| `DayMarkerDot` | En dags markering i kalender och datumremsa: `isToday` (punkt i `secondary` med ring i `progressDone.outline`, annars `outline`), `marked` (punkt i `primary`), `selected` (punkt och ring i `onPrimary`); läses inte – dagens knapp läser `dayDescription` | Flyttad från Dagbokens `DayMarker` (#68) |
| `DayPicker` | "Välj dag" i en lista över datum (Kodhäst-standard): chip med kalenderikon först i filterraden som öppnar ett ark med `AppCalendar` (`marked`, `today`, framtiden ovalbar) och knappen Idag; valet ges till `onPick`, och appen rullar listan dit med `EntityListScreen(scrollTo = …)` | Nytt (#68): ersätter Dagbokens list- och kalenderikoner i sidhuvudet |
| `DayTab` | Dagfliken i en veckorad: kolumn minst 48 dp, `shape`, `background`, `Role.Tab` (`selected`, `enabled`), `inner` (dagens egen semantik och marginal) | Flyttad från Dagboken (#76), nu publik för appens egna veckorader |
| `DayTabWeekday` | Dagflikens veckodag ("mån") i `caption`, `color` (standard `onSurfaceVariant`) | Flyttad från Dagboken (#76) |
| `DeleteConfirmDialog` | Permanent radering av en `DeleteAction` (titel, text, "Radera" i felfärg); titeln t.ex. `R.string.delete_title_format` ("Radera %s?") | Var appens egen (`DeleteAction` från `EntityEditScreen`) |
| `DiscardChangesDialog` | "Släng ändringar?" – "Släng" / "Fortsätt redigera" | Var appens egen |
| `DropdownPill` | Kompakt rullgardin: klickbar `InfoPill` med valt värde och pil som öppnar `AppMenuPopup`; `tone`, `onClickLabel` (standard "Välj"). Läses som rullgardin, minst 48 dp | Flyttad från Dagbokens `CompactDropdownButton` (#76); Dagboken skickar `tone = Tone.Neutral` |
| `DurationRow` | Tidsåtgång: `QuantityStepper` i minuter (`step`, standard 5, tak ett dygn) och snabbval (`quickPicks`, standard 15–120 min) i en `LabeledGroup` som visar "1 tim 30 min" (`DateFormat.duration`); `label` (standard "Tidsåtgång"). `DurationRowDefaults` | Flyttad från Dagboken (#57) |
| `EmojiPicker` | Välj en emoji ur `emojis` (först, som `ChoiceGrid`), `selected`, `onSelect`, med `ChoiceGrid`s ring | Listan krävs – apparnas listor (`PERSON_EMOJIS`, `DEFAULT_EMOJIS`) stannar i appen; ReseApotekets egen markering ersätts av ringen |
| `EmptyState` | Tomt läge med ikon, rubrik, text och knapp; `compact` (Dagboken); `actions: List<EmptyAction>` (en eller flera knappar i full bredd, `label`, `onClick`, `icon`, `variant`); med `AppBrandStyle(emptyStateMark = true)` `BrandMark` i ikonrutan, bara i fullstort läge utan fel | Fonders tomma lägen med flera val |
| `EntityDetailScreen` | Detaljramen: huvud, sektioner, Redigera och meny; underskärm med `title`, flikläge med `onBack = null`; `onRefresh`/`refreshing` | Union av apparnas; se Ramarna |
| `EntityEditScreen` | Redigeringsramen med `EditorState`: Spara, "Släng ändringar?", sparfel, arkivera/återställ/radera, `formError`, `header` (centrerat överst, t.ex. en stor avatar), tangentbordet | Union av apparnas; se Ramarna. `header` från ReseApotekets personformulär (#75) |
| `EntityListScreen` | Listramen: laddar, tomt, fel, grupper i kort, kolumner efter bredd, arkivering, `filter`, `footer`, `onRefresh`/`refreshing` | Union av apparnas; se Ramarna |
| `ErrorSnackbar` | Visar ett `UiError` en gång; `template` (t.ex. "Synken nekades: %1$s"), `key`, `duration` (samma ordning som `MessageSnackbar`) | Tar `UiError` i stället för appens `DataError` |
| `ExpandableContent` | Det som fälls ut: fjädrar fram (`defaultSpatialSpec`) och fälls snabbt ihop (`fastSpatialSpec`); `visible`, `modifier` | Flyttad från Dagboken (#46) |
| `ExampleChips` | Exempel att trycka in i ett fält; `label` är `@Composable` som i `ChoiceChips` och `ChoiceGrid` | – |
| `ExpandButton` | Chevronknappen för något som fälls ut utan titelrad: `AppIconButton` med chevronen som fjädrar ett halvt varv, "Fäll ut"/"Fäll ihop" och läget som `stateDescription` (som `CollapsibleHeader`) | Flyttad från Dagbokens postkort (#76) |
| `Foldout` | Hopfällbar sektion: hela titelraden växlar (`CollapsibleHeader`), `summary` under titeln i stängt läge, `trailing` (egen kontroll före chevronen, växlar inte), `header` (ersätter titeln), `contentSpacing` | Flyttad från Dagboken (#46) |
| `FavoriteMark` | Den lilla fyllda stjärnan i `primary` efter titeln på en stjärnmärkt post; läses "Favorit" | Flyttad från Dagboken (#57) |
| `FavoriteStar` | Stjärnan som markerar `name` som favorit: ikonknapp, ifylld när `favorite`, läses "Markera X som favorit"/"Ta bort X som favorit" med läget "Favorit"/"Inte favorit". `favoriteMenuItem(name, favorite, onToggle)` ger samma text och stjärna som menyrad (`AppMenuItem`) | Flyttad från Dagboken (#57), med ikonerna `ic_star`/`ic_star_filled` |
| `FieldError` | Felraden under ett fält | – |
| `GroupLabel` | Etikett över en grupp | – |
| `InfoPill` | Pill med ton; `trailingIcon`, `role`, `onClickLabel`, `colors`, `shape` (standard `AppTheme.shapes.pill`), `contentDescription` | Fonder: `shape = AppTheme.shapes.smallTile`, egna toner för Tertiary/Neutral |
| `ItemRow` | Den enda listraden: `leading`, `trailing`, `tinted`, `done`, `accent`, `navigates`, `inactive`, `below`, `titleHighlight`, `icon`, `belowAlignsWithTitle`, `overline` (ovanför titeln), `titleStyle` (standard `itemTitle`). Kapad titel: långtryck visar hela titeln och undertexten i en bubbla | Dagbokens `icon`, `belowAlignsWithTitle`; bubblan nu även i Dagboken. Avståndet titel–undertext: `LocalItemRowTextGap` (standard 2 dp; Dagboken `Spacing.xs`) |
| `ItemRowText` | Listradens titel och undertext, för appens egna kort | Dagbokens `RowText` |
| `KeyFigure` | Ett nyckeltal: `value` stort (`bigNumber`) och `label` under, båda i `tone`s innehållsfärg | Flyttad från Dagbokens `DayDoneCard` (#76) |
| `LabeledGroup` | Etikett, hjälptext och innehåll; `helperAbove` (Dagboken) | – |
| `LeadingSlot` | 48 dp-ruta för det ledande elementet, så att titlarna linjerar; `TOUCH_TARGET` (48 dp) är den minsta tryckytan för apparnas egna klickbara ytor | – |
| `rememberReducedMotion()`, `Modifier.blockInput(block)` | Om systemets animationer är avstängda; låser beröring medan `block` är sant | – |
| `LoadingDialog` | Blockerande väntan som inte går att stänga: laddningsindikatorn (som `AppLoading`, prickarna med `dotLoading`) bredvid `title` och `message`, i kortens färg och form; TalkBack läser texten när den visas. Bara för något användaren startat och måste invänta – bakgrundsarbete visas med sidhuvudets prickar | Flyttad från Fonder (#85), där den stod med indikatorn ovanför texten |
| `LoadErrorState` | Läsfel med "Försök igen" (bara om `retryable`) | Tar `UiError` |
| `MakerMark` | Kodhästs signatur: `BrandMark` och "En kodhäst-app" (`text`, sträng `maker_mark`) i `caption` och `onSurfaceVariant`, en nod som läses som texten | Ny (android-ui #28) |
| `MessageSnackbar` | Visar en text en gång i `AppSnackbarHost` | – |
| `MorphingCheck` | Krysset som morfar från ring till kaka med bock (bara visning); `interactionSource` = radens tryck, som med djup får krysset att sjunka in | – |
| `NoteIndicator` | Anteckningsikon som visar anteckningen i en dialog | – |
| `NoteCard` | Anteckningskortet i ett formulär: `AppCard` med en `Foldout` som stängd visar början av texten (eller "Lägg till en anteckning") och utfälld ett flerradigt fält; ett tömt fält ger `null`. `initiallyExpanded`, `error` (syns också stängd), `maxLength` (standard `AppTextFieldDefaults.LONG_MAX_LENGTH`; Dagboken `TextLimits.LONG`) | Flyttad från Dagboken (#57); taket är en parameter |
| `NoticeBanner` | Meddelande överst på en skärm: tonat `AppCard` (`tone`, standard `Tone.Primary`) med `icon`, `text`, `detail` och en pil; hela kortet är knappen (`onClick`, läses med `onClickLabel`, standard "Öppna"); `onClick = null` = bara ett meddelande; `action` = en egen knapp under texten (kräver `onClick`) | Flyttad från Dagboken (#54), som skickar sin `Tone.Warning`; ReseApotekets "Notiser är avstängda" |
| `PausableRow` | Rad som kan pausas: typ som pill, nedtonad "Pausat"; `pillTone` krävs | Tonen är appens (ReseApoteket `Tone.Blue`, Dagboken `Tone.Neutral`) |
| `PillRow` | Några `InfoPill` tätt i en rad som inte bryts eller rullar, t.ex. etiketterna sist i en listrad | Flyttad från ReseApoteket (#75, packlistans etiketter) |
| `PickerField` | Fält som öppnar en väljare; `emptyLabel`, `contentLabel`, `enabled` (`false` låser med värdet synligt) | – |
| `QuantityStepper` | − värde +; `step`, `valueText`, `buttonSize` (standard `LocalQuantityStepperButtonSize`: `QuantityStepperDefaults.ButtonSize` 44 dp, `TouchTargetButtonSize` 48 dp), `showLabel` (etiketten som text till höger, läses bara en gång) | Dagboken sätter `TouchTargetButtonSize` en gång i sitt tema; `showLabel` är ReseApotekets `LabeledStepper` (#75) |
| `ReminderTimeRow` | En påminnelse: namn och klockslag, tryck öppnar `TimePickerPopup`; med `onEnabledChange` en `SwitchRow` med reglaget som egen kontroll, annars en `ItemRow`. Avslagen (`enabled = false`) eller `inactive` tonar texten, inte reglaget | Flyttad från Dagboken (#57) |
| `SelectField` | Fält med rullgardin: `PickerField` med pil som öppnar en `AppMenuPopup` med `options` (`optionLabel`); `placeholder` utan val, `clearOptionLabel` (ett val överst som ger `null`), `supportingText`, `error`, `enabled = false` låser med valet synligt | Flyttad från Fonder (#85), där den var Material 3:s `ExposedDropdownMenuBox` med `OutlinedTextField` |
| `SectionCard` | Kort med rubrikrad: `AppCard` med `SectionHeader` (`title`, `icon`, `count`, `countTone`) och innehållet under; `tone` tonar kortet och rubriken, `contentColor` som i `AppCard` | Nytt (#76) för Dagbokens tre kort med rubrikrad |
| `SectionHeader` | Sektionsrubrik med antal; `countTone`; laddningsindikator med `working`; med `AppBrandStyle(sectionDots = true)` två prickar i `primary` före titeln när ikonrutan saknas; titeln högst två rader brutna mellan ord, ett ord som inte ryms kortas med "…" och visar hela titeln i en bubbla vid långt tryck (som `ItemRow`) | – |
| `SectionFoldout` | `Foldout` med `SectionHeader` i titelraden: `icon`, `count`, `tone`, `countTone`, `trailing` (t.ex. en tonad lägg till-knapp, `AppIconButton(…, variant = IconButtonVariant.Tonal)`), `emptyText` (tomraden som i en listgrupp) | Flyttad från Dagboken (#46); `trailing` är ny (ReseApotekets persondetalj); `emptyText` från ReseApoteket (#75) |
| `SettingsCard` | Kort på en inställningssida; `error` visas sist i kortet, där åtgärden gjordes | Flyttad från ReseApoteket (#75) |
| `SettingsPage` | Inställningssidans ram: liten topprad med `title` och `onBack`, innehållet rullar med toppraden, `GroupLabel` + kort med samma marginal och avstånd, luft för flikraden sist | Flyttad från ReseApoteket (#75) |
| `ShareBar` | En andel av en helhet: `label` och `value` (appens formatering) ovanför `AppProgress`-spåret fyllt till `fraction`; `target` ritar ett målstreck i `targetColor` (standard `secondary`) och med `targetValue` blir texten "52,0 % · mål 45,0 %"; `color` (standard `primary`, en restpost t.ex. `outline`). Läses som en enhet med andelen som framsteg | Fonders `ExposureBar` (#85): spåret har bibliotekets höjd (8 dp, förut 10) och färg (`ExtendedColors.track`) |
| `SignInScreen` | Inloggningen i hela skärmen: `appIcon`, `appName`, `welcome`, `note`, knappen "Logga in med Google" (`AppButton` med inloggningsikonen), `busy` ("Loggar in …", spärrad), `error` en gång som meddelande ovanför knappen (`onErrorShown`), `onSignIn` | Flyttad från ReseApoteket och Dagboken (#53); RA:s egna `LaunchedEffect` blir `ErrorSnackbar` |
| `SignOutRow` | "Logga ut" med utloggningsikonen; frågar "Logga ut?" med `ConfirmDialog` innan `onSignOut`. Likadan i alla appar: bara `appName` i texten skiljer, och bekräftelseknappen är en vanlig knapp i appens färger | Flyttad från ReseApoteket och Dagboken (#54); apparnas egna texter och RA:s röda knapp försvinner (1.15.0, Mats beslut) |
| `StatPill` | Mätvärde: ikon, värde och etikett i en tonad yta, läses som en enhet; `onClick` (knapp, minst 48 dp), `onClickLabel`, `action` (åtgärden sist, kräver `onClick`). `tone` krävs | Flyttad från Dagboken (#57); tonen är appens (Dagboken `Tone.Neutral`) |
| `StatPillGrid` | Mätvärden (`StatPillItem(icon, value, label)`) som `StatPill` i ett rutnät, `columns` per rad (standard två), alla i `tone`; `rowSpacing` (standard `Spacing.s`); ett ensamt sist tar hela bredden | Flyttad från Dagbokens `PillRows` (#76); Dagboken skickar sektionens avstånd som `rowSpacing` |
| `StepIndicator` | Stegprickarna under ett formulär i steg: den aktuella fjädrar ut till ett streck i `primary`, de andra i `ExtendedColors.track`. Läses inte | Flyttad från Dagbokens `StepDots` (#76) |
| `StepNavigation` | Föregående/Nästa och Spara på sista steget för en `PagerState`, i en rad (t.ex. arkets `footer`); `saveEnabled`, `saving`, klart-läget `done` med `doneText` och `doneAnnouncement` (live region) | Flyttad från Dagbokens `StepwiseScreeningNavigation` (#76) |
| `SuggestionField` | Textfält med förslag (`Suggestion`) i ett kort under, antalet läses upp | – |
| `SwipeBackground` | Det som syns bakom en svept rad: `icon` och `label` i högerkanten, `colors` (`ToneColors`, standard primärtonen), `shape` (standard `row`) | Ersätter bakgrunden i Dagbokens `SwipeToDelete` (`ic_delete`, felfärgen, `card`) |
| `SwipeToDelete` | Radera med svep åt vänster för ett kort som har Radera i sin meny: svepet begär radering (`onRequest`), kortet fjädrar genast tillbaka, bakgrunden "Radera" i felfärgen (`shape`, standard kortets) läses inte. Samma svep som `SwipeToHide` | Flyttad från Dagbokens postkort (#76); ingen rå `SwipeToDismissBox` kvar i appen |
| `SwipeToHide` | Arkivera/dölj med svep åt vänster, och samma åtgärd för TalkBack; bakgrunden är `SwipeBackground` med `icon` och `colors` (standard arkivera i primärtonen). Samma svep som `SwipeToDelete` | – |
| `SwitchControl` | Själva växeln, för appens kort med reglage; `enabled`, `interactionSource` (ett tryck någon annanstans, som raden, som knoppen ska följa) | – |
| `SwitchRow` | Inställningsrad med växel; `onClick` (växeln blir egen kontroll), `inactive` (tonar ned texten, inte växeln), `enabled` (låser växeln) | Dagbokens `inactive`; Fonders `enabled` |
| `TimeField` | Tidsfält (kotlinx `LocalTime`) som öppnar `TimePickerPopup` | – |
| `TimePickerPopup` | Tidsväljaren som dialog | – |
| `UnknownValueNotice` | I stället för ett värde som appversionen inte känner till (t.ex. en typ som en nyare version sparat); `text`, standard "Det här kan inte visas i den här versionen. Uppdatera appen för att ändra." | Flyttad från ReseApoteket (#75) |
| `UpdateRequiredScreen` | "Uppdatera appen": `message`, `note`, knappen "Hämta senaste versionen" som öppnar `releasesUrl` | Flyttad från ReseApoteket och Dagboken (#53) |
| `UndoSnackbar` | "Arkiverad – Ångra" för `UndoRequest`; `format`, `onRequestDismissed` | – |
| `ValueRow` | Etikett till vänster, värde till höger med tabulära siffror: `value` (`String` eller `AnnotatedString` med egna färger), `supportingLabel`/`supportingValue` (dämpade rader under), `valueColor` eller `valueTone`, `muted` (hela raden dämpad), `valueMuted` (bara värdet, t.ex. "Hämtar …"). Läses som en enhet; en lång etikett klipps efter två rader så att värdet syns | Fonders `PeriodRow` utan domänen (#85): kr/%-formateringen och vinst/förlust-färgen stannar i Fonder |
| `ValueTile` | Ett beräknat värde i en tonad ruta: `label`, `value` i stor stil, `supporting` (förklaringen), `tone` (standard `Tone.Primary`); läses som en enhet och upp igen när värdet ändras | ReseApotekets `PackingPreview` (#75), som skickar `Tone.Blue` |

**Modifierare:** `Modifier.longPress(label, onLongPress, haptic = false, semantics = LongPressSemantics.LongClick)` –
långtryck ovanpå en yta vars klick ägs av något annat (`AppFilterChip`, Dagbokens känslor): avbryts av
rörelse, tar resten av gesten efter långtrycket, `haptic` ger en puls, TalkBack får långtrycksåtgärden
(`LongClick`) eller en anpassad åtgärd (`CustomAction`); `onLongPress = null` gör ingenting.
`Modifier.copyOnLongPress(text, label, clipLabel = label)` – långtryck kopierar `text` till urklipp med haptisk
puls (Android visar själv bekräftelsen), TalkBack-åtgärden heter `label` ("Kopiera fondnamn"); samma gest som
`longPress`, så att en klickbar rad eller ett kort inte också öppnas. Fonders `copyableFundName` (#85).

Strängarna (svenska) och ikonerna ligger i `ui-components/src/main/res`. En app som har en
resurs med samma namn (t.ex. `ic_add` eller `more_options`) ersätter bibliotekets – så behåller
varje app sin ikonstil utan parameter.

## Bygga och testa

```bash
./gradlew testDebugUnitTest          # Robolectric-beteende, UiConsistencyTest, temat, kontrakten i ui-testing
./gradlew verifyRoborazziDebug       # skärmdumpar mot ui-components/src/test/screenshots
./gradlew recordRoborazziDebug       # spela in referenserna – bara vid avsiktlig visuell ändring
./gradlew cpdCheck                   # copy-paste-detektor (PMD CPD, 80 tokens)
./gradlew spotlessApply              # kodstil (ktlint + compose-rules), före push – inte i CI
./gradlew publishToMavenLocal        # prova en app mot en lokal version
```

Robolectric (SDK 36) kräver JDK 21; koden byggs för Java 17. Skärmdumparna tas med ett testtema
med ReseApotekets palett (`ui-components/src/test/.../testing/TestTheme.kt`).

## Publicering

Versionen står i `gradle.properties` (`version=`). `.github/workflows/publish.yml` publicerar alla fem
artefakterna till GitHub Packages, antingen när en tagg `v<version>` pushas eller manuellt
(Actions → Publicera → Run workflow på `main`), som då skapar taggen själv. Molnsessioner kan inte
pusha taggar och använder därför den manuella vägen.

## Byta från 1.x till 3.0

2.0.0 samlade serien från granskningen 2026-11-18 (#73–#77); 3.0.0 tog bort formerna ur `AppTheme` direkt
efter, innan någon app bytt. Byt direkt från 1.x till 3.0.0. Det som bryter, och vad appen gör:

- **Former:** ta bort `shapes = …` ur appens `AppTheme(…)` – parametern finns inte längre, och alla appar får
  bibliotekets former med 22 dp-kort. ReseApoteket går från 28 till 22 dp, Fonder från sina egna radier (kort och
  rader 18 dp, fält och menyer 12 dp) till bibliotekets (Mats beslut 2026-11-18).
- **Borttaget:** `SignInButton`, ikonen `ic_google`, strängen `sign_in` och den dolda `SignOutRow(onSignOut, modifier,
  message, destructive)` – ingen app använde dem; använd `SignInScreen` och `SignOutRow(appName, onSignOut)`.
- **`EmojiPicker(emojis, selected, onSelect)`:** listan först, som `ChoiceGrid` (förut `EmojiPicker(selected, onSelect, emojis)`).
- **`ErrorSnackbar(…, template, key, duration, onShown)`:** `key` före `duration`, som `MessageSnackbar`. Anrop som
  skickar `duration` utan namn byter till `duration = …`.
- **`ExampleChips(label)`** är `@Composable (T) -> String`, som i `ChoiceChips` och `ChoiceGrid`; ett vanligt lambda
  fungerar som förut.
- **`ExpandableContent(visible, modifier, content)`:** ny `modifier`; anrop med avslutande lambda påverkas inte.
- **Ikonstorlekar:** `IconSize` i `ui-theme` (Dagbokens `IconSize` byter till den: `marker`, `pill`, `row` har samma namn
  och värden). Byt egna dp-värden för ikoner mot den.
- **`AppTheme.transparent`, `AppShapes.fab`, `AppShapes.dialog` och `PROGRESS_STROKE`** är nya; `ui-forbidden.txt`
  fäller nu `val ICON = …dp`, `strokeWidth = <tal>`, `colorScheme.x.copy(alpha = 0f)`, `XxxDefaults.shape` och
  `MaterialTheme.shapes` i biblioteket. Apparna som kopierat mönstren kan ta med de nya raderna.
- Det som kom i #73–#76 (nya komponenter, parametrar och djup) står i versionshistoriken nedan; det bryter inget.

## Versionshistorik

| Version | Ändring |
|---|---|
| 3.1.0 (under arbete) | Fonders generella delar (#85). Versionen höjs och publiceras i L3 |
| ↳ #85 L1 | Nya `ValueRow`, `ShareBar`, `SelectField`, `LoadingDialog` och `Modifier.copyOnLongPress` (från Fonder); `PickerField(enabled)`. En laddningsindikator (Mats 2026-11-19): med `AppBrandStyle.headerDots` står prickarna också före rubriken i `AppTopBar` och hoppar medan `LocalTopBarSyncing` är sant (ny parameter `syncing`) – **ReseApotekets och Dagbokens underskärmar och formulär får prickarna** när de byter till 3.1.0, eftersom båda har `headerDots`. Nya skärmdumpar `ValueRow_lagen`, `ShareBar_andelar`, `SelectField_lagen`, `SelectField_meny`, `LoadingDialog_standard`, `AppTopBar_prickar`, `AppTopBar_prickar_hamtar`; befintliga oförändrade (testtemat har inte `headerDots`) |
| 3.0.0 | Formerna är ingen appinställning (Mats 2026-11-18, också för Fonder): `AppTheme` tar inte längre `shapes`, och alla appar får `AppShapes()` med 22 dp-kort. Publicerad direkt efter 2.0.0, innan någon app bytt – apparna byter från 1.x direkt till 3.0.0. Inga skärmdumpar ändrade |
| 2.0.0 | Serien från granskningen 2026-11-18 (#73–#77), en major så att apparna byter en gång. Brytande ändringar och vad appen gör: se Byta från 1.x till 3.0 |
| ↳ #77 | API-städning: `SignInButton`, `ic_google`, `sign_in` och dolda `SignOutRow` borttagna; `EmojiPicker(emojis, selected, onSelect)`; `ErrorSnackbar` med `key` före `duration`; `ExampleChips(label)` `@Composable`; `ExpandableContent(modifier)`; ikonstorlekarna `IconSize` i `ui-theme` i stället för privata konstanter i nio filer; `PROGRESS_STROKE`, `AppTheme.transparent`, `AppShapes.fab` och `AppShapes.dialog` i stället för hårdkodade värden och Material 3:s standardformer, och `ui-forbidden.txt` fäller dem. Formerna samma i alla appar: `AppShapes` har standardvärdet 22 dp-kort och `AppTheme(shapes)` är valfri (borttagen i 3.0.0). Testtemat använder standardformerna (förut 28 dp-kort), så skärmdumparna med kort, rader i kort och dialoger är omtagna; `SignInButton_standard` och `AppHeader_utloggat` borttagna |
| ↳ #75 | ReseApotekets generella delar: `ValueTile` (packlistans förhandsvisning), `UnknownValueNotice` ("kan inte visas i den här versionen"), `PillRow`, `SettingsPage` och `SettingsCard(error)`; nya parametrar `QuantityStepper(showLabel)`, `AppBottomSheet(subtitle)`, `EntityEditScreen(header)` och `SectionFoldout(emptyText)` (med listgruppens tomrad). Standardvärdena ändrar inget; inga befintliga skärmdumpar är omtagna |
| ↳ #76 | Dagbokens generella delar: nya `AppSlider`, `DateStrip`, `DayTab`, `DayTabWeekday`, `SwipeToDelete`, `ExpandButton`, `StepIndicator`, `StepNavigation`, `DropdownPill`, `SectionCard`, `KeyFigure` och `StatPillGrid`; `ChoiceChips(more, moreLabel, morePickLabel)`; spårets höjd `TRACK_HEIGHT` (8 dp) i `ui-theme`, som `AppProgress` nu läser. Befintliga skärmdumpar oförändrade |
| ↳ #73 | Rättelser: FAB-menyns Stäng-knapp i huvudknappens färg (`ActionColor.Tertiary` gav förut gråbrunt kryss); dra-för-att-uppdatera även i laddning, tomt och fel i `EntityListScreen` och `EntityDetailScreen`; hopfällt läge per grupp i stället för per rubrik; "Hämta senaste versionen" i `UpdateRequiredScreen` visar ett meddelande i stället för att krascha utan webbläsare; `QuantityStepper`:s 48 dp tryckyta med 44 dp knappar bevakas av ett test (den fanns redan, genom Compose utvidgade tryckyta); klickbara kort läses som knapp, ny parameter `AppCard`/`CardSegment(onClickLabel)`, och lyft och tryckskala bara på klickbara kort (`NoticeBanner` använder kortets klick, så ett klickbart meddelande får med temats djup kortets skugga och tryckskala, som förut klipptes bort); `TabBackStack.entries` byggs inte om vid varje läsning; README:s version och artefaktantal rättade och bevakade av `UiConsistencyTest`, som också kräver ett beteendetest per publik komponent. Nya skärmdumpar `AppFloatingToolbar_fabmeny_tertiary` och `NoticeBanner_djup`; `NoticeBanner_lagen` omtagen (kantutjämning, högst 9/255 i en kanal) |
| ↳ #74 | Djup på det som saknade det (Mats "look and feel ska vara samma"): kalenderns valda dag (som ett valt chip) och alla dagar krymper nedtryckta; huvudknappen i `AppFloatingToolbar`, FAB-menyns val och Stäng-krysset får den fyllda knappens skugga, skimmer och ljuskant som sjunker in vid tryck (huvudknappen utan Material 3:s skugga med djup); flikar och segment krymper nedtryckta och det valdas skimmer tonar vid tryck; `ChoiceGrid`:s celler krymper, ringens skugga sjunker in och ett valt utan ring får skimmer; `MorphingCheck(interactionSource)` (ny parameter, `CheckRow` skickar sitt tryck) sjunker in; `AddSplitButton(style)` (ny parameter) med meny har den tonade `AppButton`:s färger (`AppButtonStyle.secondaryTone`), höjd och djup – skuggan under hela paret; datumväljaren och `AppBottomSheet` får kortens kant; klickbara `InfoPill` och `StatPill` krymper; `AppSlider` får sänkt spår och lyft knopp. En hjälpare för fylld yta (`depthSurface`) delas av knappar, ikonknappar, chips och allt ovan. Inte gjort: `AppMenuPopup`:s tonade skugga – Material 3:s meny ritar sin skugga utanför popupfönstret och den kan inte tonas, och en egen skugga klipps av fönstret. Utan djup oförändrat, utom `AddSplitButton` med meny, som nu följer knappstilens höjd och tonade färg. Omtagna: `Valda_djup`, `AppFloatingToolbar_stilar`, `AppFloatingToolbar_huvudknappfarg`; nya `Element_djup`, `FabOchKalender_djup_nedtryckt`, `AppFloatingToolbar_fabmeny_djup`, `AppBottomSheet_djup`, `DatePicker_djup` |
| 1.21.0 | Reglaget får djup som knapparna (#70, Mats val A): med temats djup får `SwitchControl` i påslaget läge skimmer och ljuskant på spåret, och knoppen lyfts med knapparnas skugga i två lager och sjunker in när den trycks; avslaget ligger platt i sitt sänkta spår, som ett chip som inte är valt. Ny parameter `SwitchControl(interactionSource)`, som `SwitchRow` använder så att knoppen sjunker in också när raden trycks. Utan djup oförändrat (raden skickar bara sitt tryck vidare med djup). Omtagna skärmdumpar: de med reglage och djup (`Kontroller_djup`), nya `SwitchControl_djup_nedtryckt` |
| 1.20.0 | `ListScrollRequest(message)`: listramen visar ett meddelande som snackbar när den rullar på begäran – Dagbokens "Inga poster den …" när Välj dag går till en annan dag (#68). Standardvärdet ändrar inget |
| 1.19.0 | Välj dag (#68, Mats val B): `DayPicker` (chip och kalenderark), `AppCalendar` med `DayMarkerDot` och `dayDescription` (flyttade från Dagboken) och `EntityListScreen(scrollTo, onScrolled)` som rullar till en rad. Standardvärdena ändrar inget utseende |
| 1.18.0 | Komponentgalleriet (#56): `ComponentGallery(onBack, appSections)` och `GallerySection` i `ui-navigation` (paketet `se.kodhast.ui.navigation.gallery`) – bibliotekets sektioner med varje publik komponent, sedan appens egna. Ligger i `ui-navigation` och inte i `ui-components`, eftersom galleriet också visar `AppNavHost`. `UiConsistencyTest` kräver att varje publik komponent finns i galleriet. Inga befintliga skärmdumpar är omtagna |
| 1.17.0 | Dagbokens generella komponenter (#57): `StatPill` (`tone` krävs), `FavoriteStar`/`favoriteMenuItem`/`FavoriteMark` (ikonerna `ic_star`/`ic_star_filled`), `DateTimeRow`, `DurationRow`, `ReminderTimeRow` och `NoteCard` (`maxLength` som parameter) i `ui-components`; `EditorSheet`/`EditorSheetState` (formulär i ark, med appens `ErrorMapper`) och `DateFormat.duration`/`durationSeconds` i `ui-common`. Regel 2 i `CLAUDE.md`: det som hör till en apps domän stannar i appen, allt annat UI finns här. Inga befintliga skärmdumpar är omtagna |
| 1.16.1 | Fix: `ItemRow(icon = …)` ritar ikonen i `onSurfaceVariant` (som pilen) i stället för `LocalContentColor`, som är svart på skärmar utan `Surface` – Logga ut-ikonen syntes inte i mörkt läge. Omtagna skärmdumpar: raderna med `icon` (bl.a. `ItemRow_varianter`, `SignOutRow_*`) |
| 1.16.0 | Framstegsraden `AppProgress` (#55), en form för båda apparna (Mats beslut, form A): texten ovanför ett rundat spår i appens färger. `doneDescription` för en egen talad text vid klart. Nya temafärger `ExtendedColors.track` och `ExtendedColors.progressDone` (`ProgressDoneColors`), båda med standard `null` (`primaryContainer` respektive `primary` utan kontur). Inga befintliga skärmdumpar är omtagna |
| 1.15.0 | `SignOutRow(appName, onSignOut)`: frågan är likadan i alla appar, bara appens namn skiljer ("Det du sparat i … finns kvar i ditt konto …"), och bekräftelseknappen är en vanlig knapp. Formen från 1.14.0 med `message` och `destructive` är dold (ingen app använde den) och tas bort i nästa major. Skärmdumparna `SignOutRow_*` är omtagna |
| 1.14.0 | Inställningarnas delar (#54): `AccountRow`, `SignOutRow`, `AboutCard`, `NoticeBanner` (flyttad från Dagboken, standardtonen är `Tone.Primary`) och `rememberNotificationAccess`/`NotificationAccess`/`Context.canPostNotifications()` – flyttade från ReseApoteket och Dagboken, som hade var sin kopia. Texterna och ikonen `ic_logout`. Inga befintliga skärmdumpar är omtagna |
| 1.13.0 | Inloggningen (#53): `SignInScreen`, `UpdateRequiredScreen` och `AuthGateHost` i `ui-components`, `AuthGate` i `ui-common`, texterna och ikonerna `ic_login`/`ic_download` – flyttade från ReseApoteket och Dagboken, som hade var sin kopia. `SignInButton` är föråldrad (oanvänd) och tas bort i nästa major. Inga befintliga skärmdumpar är omtagna |
| 1.12.0 | Ny modul `ui-navigation` (#52): `TabBackStack`/`rememberTabBackStack` (flikarnas back stackar, sparade i samma format som apparnas `AppBackStack`), `AppTransitions` och `AppNavHost` – flyttade från ReseApoteket och Dagboken, som hade var sin kopia. `SyncUiState` i `ui-common`. Inga befintliga skärmdumpar är omtagna |
| 1.11.0 | Gemensamma verktyg ur apparna (#51): `DateFormat`, `WriteFailures` och `screenSharing` i `ui-common` (nytt beroende där: kotlinx-datetime, som `ui-components` redan hade), `rememberReducedMotion()`, `Modifier.blockInput` och den publika `TOUCH_TARGET` i `ui-components`, och `AppFilterChip(leading)` för ett eget ledande element. `DateField`/`TimeField` formaterar med `DateFormat` (samma text som förut). Inga befintliga skärmdumpar är omtagna |
| 1.10.0 | För packlistan i ReseApoteket (#172): `ListGroup.action` (`GroupAction`, plus i rubriken), `ListGroup.fold` (`GroupFold`, hopfällt läge utifrån), `ListGroup.emptyText` och `EntityListScreen.groups` (grupper som alltid visas, också i en tom lista), `EntityListScreen.menu` (en ⋮ med arkivväxeln sist), `ListArchive.showLabel`/`hideLabel`/`toggleIcon` och `ArchiveActions.showing`. `SectionHeader`-titeln står på högst två rader brutna mellan ord; ett ord som inte ryms kortas med "…" och visar hela titeln vid långt tryck – bubblan är utbruten ur `ItemRow` (`FullTextBubble`). Rubrikändringen gäller alla appar men ändrar ingen befintlig skärmdump i biblioteket; övriga standardvärden bevarar utseendet |
| 1.9.0 | `AppBrandStyle.actionColor` (`ActionColor.Secondary` standard, `Tertiary`) och parametern `actionColor` i `AppFloatingToolbar`: huvudknappens färg, för en app vars `secondary` är neutral (ReseApotekets blåa plusknapp). Standardvärdet bevarar dagens utseende |
| 1.8.1 | `EntityListScreen`: exemplen i tomt läge (`EmptyContent.examples`) syns också utan lägg till-knapp (`add = null`) – för en flik med plusmeny (ReseApotekets Gemensamt). Ingen app hade exempel utan knapp, så inget utseende ändras |
| 1.8.0 | Kontot och de hopfällbara sektionerna flyttas från Dagboken (#46): `AccountAvatar(photoUrl)` laddar profilfotot med Coil 3.6.3 och cachar det på disk; `ProfilePhotoCache`/`CoilProfilePhotoCache` tömmer cachen vid utloggning och `NetworkStatus`/`ConnectivityMonitor` ger anslutningspluppen (paketet `se.kodhast.ui.account`). `Foldout`, `SectionFoldout` (ny `trailing`) och `ExpandableContent`. Nytt beroende: Coil (`coil-compose`, `coil-network-okhttp`) och behörigheterna `INTERNET`/`ACCESS_NETWORK_STATE` i bibliotekets manifest. Inga befintliga skärmdumpar är omtagna |
| 1.7.0 | Kodhäst-sidfoten (#44): `ToolbarStyle` (`Toolbar`, `Surface`, `Tonal`, `Filled`) och `ActionMenuStyle` (`Popup`, `Fab`) som parametrar på `AppFloatingToolbar` och som `AppBrandStyle.toolbarStyle`/`actionMenu`. FAB-menyn: valen som kapslar i `primary` ovanför huvudknappen, som blir ett kryss. Standard är dagens utseende; inga befintliga skärmdumpar är omtagna. Se Sidfoten |
| 1.6.0 | Kodhäst-sidhuvudet (#42): `AppHeader` (144 dp i vila, 64 dp med kortfärg och skugga rullat, rubriken flyttas upp till appikonen, prickar som hoppar vid synk), `AccountAvatar` (flyttad från Dagboken, med `online`-plupp), `SignInButton`, `AppIdentity`/`LocalAppIdentity`, `LocalTopBarAccount`, `LocalTopBarSyncing`, `AppBrandStyle.headerDots`, `ExtendedColors.online` och `AppDepth.headerShadow`. Ramarnas flikläge ritar sidhuvudet bara när appen satt `LocalAppIdentity`; utan den är allt som förut och inga befintliga skärmdumpar är omtagna. Se Sidhuvudet för hur en app slår på det |
| 1.5.0 | Kortens skimmer (#40): `AppDepth(cardSheen = …)` ger kort i kortfärgen ett svagt skimmer som tonar ut över de översta 64 dp – inte tonade kort eller kort med egen kontur, och i en lista av bitar bara den översta. Standard `null` bevarar dagens utseende; inga befintliga skärmdumpar är omtagna |
| 1.4.1 | Rättning för #35: med djup satt sig `modifier` på knappar, chips och segmentval på en ruta utanför elementet, så att en apps semantik (`stateDescription`, `testTag`) inte hamnade på den klickbara noden och TalkBack inte läste den. Tryckytan är nu en layoutmodifierare i elementets egen kedja; utseendet är oförändrat och inga skärmdumpar är omtagna |
| 1.4.0 | Djup och tryckkänsla, del 2 av #35: med `AppTheme.depth` får kryssets bock, stegaren, växeln, det valda i färg- och emojiväljaren, fältet i fokus (fokusring), meddelandet, menyn och dialogerna djup (se Djup under ui-theme). `AppTextField` byggs nu på `BasicTextField` med Material 3:s `DecorationBox`, så att fokusringen kan ritas utanför kanten – utseende och beteende utan djup är desamma. Standard `depth = null` bevarar dagens utseende; inga befintliga skärmdumpar är omtagna |
| 1.3.0 | Djup och tryckkänsla, del 1 av #35: `AppTheme(depth = AppDepth(…))` ger knappar, ikonknappar, valda chips och segment, verktygsradens valda flik och kort ljuskant, skimmer, skugga och nedtryckt läge (se Djup under ui-theme). Standard `depth = null` bevarar dagens utseende; inga befintliga skärmdumpar är omtagna |
| 1.2.0 | För Fonder (#33): `ToolbarAction` får `menu` (`List<AppMenuItem>`), så att huvudknappen i `AppFloatingToolbar` fäller ut en meny i stället för att anropa `onClick`; knappen läses då "Hopfälld"/"Utfälld". Dra för att uppdatera blir publik som `AppPullToRefresh` (förut intern i ramarna), för skärmar utan ram. Befintliga konstruktoranrop (`ToolbarAction(label, icon) { … }`) och utseendet är oförändrade (destrukturering och positionell `copy` av `ToolbarAction` ändras, eftersom `menu` står före `onClick`; ingen app använder dem); inga befintliga skärmdumpar är omtagna |
| 1.1.0 | Kodhäst-delarna (#28): `BrandMark` (märket) och `MakerMark` (signaturen "En kodhäst-app"), och `AppBrandStyle(sectionDots, dotLoading, emptyStateMark)` med `LocalAppBrandStyle`, som slår på prickar före sektionsrubriker utan ikonruta, prickar som laddning i `AppLoading` och sektionsrubrikens `working`, och märket i `EmptyState`. Allt är avstängt som standard: utseendet är oförändrat tills appen slår på det, och inga befintliga skärmdumpar är omtagna. Nytt `res/drawable/kodhast_branding.xml` (ordmärket till startskärmen, 200 × 80 dp) med färgerna `kodhast_branding_text` och `kodhast_branding_dots` (standard Material 3:s `onSurface`), som appen ersätter i `values/` och `values-night/` |
| 1.0.0 | Repot har flyttat till organisationen Kodhast. Gruppen och Kotlin-paketen heter nu `se.kodhast.ui` (förut `se.partee71.ui`), och adressen är `https://maven.pkg.github.com/Kodhast/android-ui`. API, beteende och utseende är oförändrade: inga skärmdumpar är omtagna. Versionen är brytande eftersom apparna måste byta grupp och importer |
| 0.10.0 | `AppTopBar` och `EntityDetailScreen` får `titleModifier`, som läggs på toppradens rubrik (efter rubriksemantiken) – t.ex. för att Fonder ska kunna göra fondnamnet kopierbart. Standard ingenting, utseendet är oförändrat |
| 0.9.3 | `AppFilterChip` är alltid en rad högt: en lång etikett kortas med ellips i stället för att trycka ihop värdet eller den väntande texten (t.ex. "tagen 06:37") så att chipet blev en hög oval. Korta etiketter ser ut som förut |
| 0.9.2 | Förenklingar utan ändrat API, beteende eller utseende: en tryckyta (`TOUCH_TARGET`) i stället för fyra egna 48 dp; `EditorState` har en vakt för spara/arkivera/radera; `ListLoader` och `DetailLoader` delar omstart och delning; `QuantityStepper`, chipfärgerna och ångra i `ArchiveActions` utan kopierad kod; `cardSegment` och `card` skapas en gång; `ArchiveActions.visible` sorterar bara när arkiverade visas; `ChoiceGrid` delar inte upp raderna vid varje rekomposition |
| 0.9.1 | Rättningar från en granskning: Ångra får sina 5 s från att meddelandet syns, inte medan det väntar bakom ett annat; två grupper med samma rubrik kraschar inte listan; "Försök igen" under en pågående läsning visar inte den avbrutna som läsfel; formulärets meny (arkivera, radera) visas först när det lagrade är läst; `Confetti` med tom `colors` tar appens färgserie; `Modifier.longPress` avbryts när komponenten själv förbrukar gesten. Utseendet är oförändrat |
| 0.9.0 | Det Dagboken har egna kopior av blir publikt: `CollapsibleHeader`, `expandLabels`/`ExpandLabels` och `chevronRotation`; `Modifier.longPress` med `haptic` och `LongPressSemantics` (och en gest som inte startar om när lambdan byts); `SwipeBackground` och `SwipeToHide` `icon`/`colors`; `LocalEditScreenMargin`, `LocalDetailSectionGap` och `LocalQuantityStepperButtonSize` som standard för `horizontalMargin`, `sectionGap` och `buttonSize`. Standardvärdena bevarar utseendet |
| 0.8.0 | `SectionHeader.working` (och `workingDescription`): laddningsindikator efter rubriken medan sektionen hämtar data – för Fonders avsnittsrubriker. Standardvärdet bevarar utseendet |
| 0.7.0 | För Fonders flikar: knappar i tomt läge utan lägg till (`EmptyAction`, `EmptyContent.actions`, `EmptyState.actions`), egen felrubrik i listan (`EntityListScreen.errorTitle`), tillbakapilens stil (`TopBarBackStyle` via `LocalTopBarBackStyle` / `AppTopBar.backStyle`) och `AppIconButton.tone`; listkontraktet prövar `emptyActions` och `errorTitle`. Standardvärdena bevarar utseendet |
| 0.6.0 | Det Fonder behöver för att byta till biblioteket utan att ändra utseende: `ButtonVariant.Outlined`, `AppButton.contentDescription` och knappstilen `AppButtonStyle` (höjd, formmorf, tonad färg) via `LocalAppButtonStyle`; `AppCard.onClick` och kortstilen `AppCardStyle` (kontur, luft, delad kontur mellan bitar) via `LocalAppCardStyle`; `ItemRow` `overline`/`titleStyle`; `CheckRow` `done`/`tinted`; `SwitchRow.enabled`; `InfoPill` `shape`/`contentDescription`; `ToolbarItem.badgeCount`; dra för att uppdatera (`refreshing`/`onRefresh`) i `EntityListScreen` och `EntityDetailScreen`. Standardvärdena bevarar utseendet |
| 0.5.0 | `AppFilterChip` får `value`, `emphasized` och `pending` (`ChipPending`), `AppMenuItem` får `supporting`, `supportingTone` och `muted` – för Dagbokens symptomstyrka och vid behov-markeringar. Standardvärdena bevarar utseendet |
| 0.4.0 | Ramarna `EntityListScreen`, `EntityEditScreen` och `EntityDetailScreen` med båda apparnas parametrar, `ArchiveActions`/`ListArchive` för arkivering, och modulen `ui-testing` med kontraktstesterna för apparnas skärmar. Kolumner efter bredd och tangentbordshanteringen gäller båda apparna |
| 0.3.0 | 19 komponenter till, som glidit isär mellan apparna, i en version med båda apparnas parametrar: listrader (`ItemRow` med bubbla vid kapad titel, `CheckRow`, `PausableRow`, `SwitchRow`), `SuggestionField`, `AppBottomSheet` med "Släng ändringar?" och fast knapprad, `AppMenu` med avdelningar och kryssrader, `AppFloatingToolbar` med huvudknapp, `ConfirmDialog`, `DeleteConfirmDialog`, `DiscardChangesDialog`, `DateField` med rensa-knapp och `EmojiPicker` |
| 0.2.0 | Modulen `ui-common` (fel-API, list-, detalj- och formulärtillstånd) och 22 komponenter till: fält, val, kort, pills, snackbars, tomt läge och läsfel; flerval i `ChoiceChips` |
| 0.1.0 | Första versionen: tema-API:t och elva komponenter ur ReseApoteket och Dagboken |
