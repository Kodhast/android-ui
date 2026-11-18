# android-ui

Gemensamt Compose-bibliotek för Kodhästs Android-appar (ReseApoteket och Dagboken, senare
eventuellt Fonder): ett tema-API och de delade komponenterna, så att samma element ser ut och
beter sig likadant i alla appar och bara finns på ett ställe. Varje app behåller sina egna
färger, former och typsnitt och skickar in dem i temat.

**Version:** 1.1.0 · Material 3 `1.5.0-beta01` (Expressive) · Compose BOM `2026.09.00` ·
Kotlin `2.4.20` · AGP `9.4.1` · compileSdk 37 · minSdk 30.

## Moduler

| Artefakt | Paket | Innehåll |
|---|---|---|
| `se.kodhast.ui:ui-theme` | `se.kodhast.ui.theme` | `AppTheme`, `Spacing`, `AppShapes`, `AppTypography`, `ExtendedColors`, `Tone`, `ToneColors` |
| `se.kodhast.ui:ui-common` | `se.kodhast.ui.common` | ramarnas tillstånd utan UI: `UiError`/`ErrorMapper`, `ListUiState`, `DetailUiState`, `EditorState`, `EditorLoader`, `shownAfter` |
| `se.kodhast.ui:ui-components` | `se.kodhast.ui.components` | komponenterna och ramarna nedan (drar in `ui-theme` och `ui-common`) |
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

och i versionskatalogen/byggfilen `implementation("se.kodhast.ui:ui-components:1.1.0")` och
`testImplementation("se.kodhast.ui:ui-testing:1.1.0")`.

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
    shapes: AppShapes,
    typeScale: Typography,                                     // appens typskala med appens typsnitt
    typography: AppTypography = AppTypography.from(typeScale), // de namngivna stilarna
    motionScheme: MotionScheme = MotionScheme.expressive(),
    depth: AppDepth? = null,                                   // djup och tryckkänsla; null = platt
    content: @Composable () -> Unit
)

AppTheme.extendedColors   // card, rowTint, toolbar, onToolbar, tones, accents, online
AppTheme.shapes           // card, cardSegment(top, bottom), row, pill, field, menu, sheet, smallTile, iconTile
AppTheme.typography       // screenTitle, bigNumber, headline, sectionTitle, quantity, itemTitle, itemSubtitle, body, pill, caption, button
AppTheme.tone(tone)       // ToneColors(container, content)
AppTheme.depth            // AppDepth?: highlight, sheen, shadow, cardEdge, cardShadow, pressedScale, cardSheen, headerShadow
Spacing.xs … Spacing.xxl  // 4, 8, 12, 16, 24, 32 dp
```

- **Toner:** `Tone` är en nyckel. Biblioteket känner bara `Tone.Primary`; appen lägger till sina
  som tillägg, t.ex. `val Tone.Companion.Green get() = Tone("green")`, och ger dem färger i
  `ExtendedColors.tones`. En ton utan färger får primärtonen.
- **Former:** bara `cardRadius` måste anges (ReseApoteket 28 dp, Dagboken 22 dp); övriga former
  har apparnas gemensamma värden som standard.
- **Accentfärger:** `ExtendedColors.accents` är appens dekorativa färgserie (personfärger,
  valbara färger) – används av `Confetti`.
- **Ansluten:** `ExtendedColors.online` är pluppen på `AccountAvatar(online = true)`; standard `null` =
  `colorScheme.primary` (Dagboken sätter sin gröna).
- **Djup (#35):** med `depth = AppDepth(…)` får knappar en ljuskant upptill, ett svagt skimmer och
  en tonad skugga, och sjunker in (krymper till `pressedScale`, skuggan tonar bort) när de trycks.
  Valda chips, segment och verktygsradens flik får ljuskanten och skimret, tonade ikonknappar en
  liten skugga, och kort en mjuk skugga och en hårfin kant (`cardEdge`; inte på tonade kort eller kort
  med egen kontur). Med `cardSheen` får samma kort ett svagt skimmer som tonar ut över de översta 64 dp
  (i en lista av bitar bara den översta; standard `null` = inget). Bitar av ett kort (`CardSegment`) ritas som ett kort utan skarvar. Den ifyllda
  bocken i `MorphingCheck`/`CheckRow` får ljuskant och en liten skugga; `QuantityStepper` får ett sänkt
  spår och upphöjda knappar som krymper nedtryckta; `SwitchControl`/`SwitchRow` ett sänkt spår och en
  liten skugga under den påslagna knoppen; det valda i `ChoiceGrid` med ring (`ColorSwatchPicker`,
  `EmojiPicker`) en liten skugga. Fältet i fokus (`AppTextField` och fälten som bygger på det) får en
  mjuk ring i primärfärgen utanför kanten. Meddelandet i `AppSnackbarHost` flyter med skugga och
  hårfin kant; `AppMenuPopup`, `ConfirmDialog` och `TimePickerPopup` får den hårfina kanten. Listrader,
  pills, rubriker och tomma lägen är platta med avsikt. Färgerna ger appen per tema; utan `depth` är
  allt som förut.
- Det som bara en app har (t.ex. Dagbokens energiskala, sömnstadier och känslotoner, `IconSize`)
  stannar i appen.

Appens tema blir en tunn omslutning:

```kotlin
@Composable
fun ReseApoteketTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) =
    AppTheme(
        colorScheme = if (darkTheme) AppColors.dark else AppColors.light,
        extendedColors = if (darkTheme) AppColors.darkExtended else AppColors.lightExtended,
        shapes = AppShapes(cardRadius = 28.dp),
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
- **`Flow<Boolean>.shownAfter(delay)`** – visar en indikator först efter en fördröjning.

## Ramarna

Varje listskärm, redigeringsskärm och detaljskärm i apparna byggs på en av tre ramar, så att laddning,
tomt läge, fel, "Försök igen", "Släng ändringar?", sparfel och arkivering beter sig likadant överallt.

```kotlin
EntityListScreen(title, state: ListUiState<T>, empty: EmptyContent, add: AddAction?, key, …,
    onRetry, group: ((T) -> ListGroup)?, archive: ListArchive?, subtitle, onBack, actions, header,
    subgroup, filter, topBarSize = TopBarSize.Large, footer, refreshing = false, onRefresh = null,
    errorTitle = null, row)
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
| Dra för att uppdatera | `onRefresh` (standard `null` = ingen gest) och `refreshing` i listan och detaljen; Expressive-indikatorn överst, ytan har taggen `ENTITY_REFRESH_TAG`; samma yta finns fristående som `AppPullToRefresh` | – | – (Fonder: Fondsök, Portfölj, Fonddetalj; Översikt, Innehav och Beslut med `AppPullToRefresh`) |

- **Arkivering:** `ArchiveActions(scope, errors, setArchived, undoFormat, errorMessage)` är logiken i
  listans ViewModel (svep, Ångra, "Visa arkiverade", `offerUndo` för annat än arkivering);
  `visible(source) { it.archived }` filtrerar och `collectAsListArchive()` ger listans `ListArchive`.
  Raderna sveps med `SwipeToHide`; `UndoSnackbar` visas av listramen.
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
    LocalTopBarAccount provides {
        if (user != null) {
            AccountAvatar(user.name, onClick = openAccount, online = online, photoUrl = user.photoUrl)
        } else {
            SignInButton(onClick = signIn)
        }
    }
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
5. **Synken:** med `headerDots` visas den av prickarna (TalkBack: "Ändringar väntar på servern"); en app
   som behåller en egen indikator sätter den som `LocalTopBarStatus` som förut.
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
    actionMenu = ActionMenuStyle.Fab       // plusknappens val växer upp ur knappen
)
```

- `ToolbarStyle`: `Toolbar` (standard, `extendedColors.toolbar`), `Surface` (kortens yta), `Tonal` (Kodhäst-standard:
  `primaryContainer`/`onPrimaryContainer`, Material 3 Expressives "vibrant") och `Filled` (`primary`).
- `ActionMenuStyle`: `Popup` (standard, rullgardin) och `Fab` (Kodhäst-standard: valen som kapslar i `primary` med
  ikon från `AppMenuItem.icon`, knappen blir ett kryss "Stäng", ljus slöja över innehållet; tryck utanför, bakåt
  eller ett val stänger).
- Plusknappens meny är `ToolbarAction(label, icon, menu = listOf(AppMenuItem(…)))`; ett eget ark för valen behövs inte.
- Utan inställning ser raden ut som förut.

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
| `AccountAvatar` | Kontots avatar: `name` (initialer, "Anna Berg" → "AB"; utan namn en person-ikon), `onClick` (`null` = bara en bild utan semantik), `online` (`true` plupp i `ExtendedColors.online` och "Ansluten", `false` "Utan anslutning", `null` inget läge), `photoUrl` (profilfotot ovanpå initialerna, laddat och cachat på disk med Coil), `photo` (egen slot, går före `photoUrl`). 36 dp i 48 dp tryckyta, "Konto och inställningar" | Flyttad från Dagboken (#42); `photoUrl` och cachen flyttade från Dagboken (#46) |
| `AddSplitButton` | "Lägg till ▾" – huvudåtgärd och pil med fler val | Menyn är en lucka: `menu = items.asSplitMenu()` (ReseApoteket) i stället för Dagbokens `menuItems` |
| `AppBottomSheet` | Ark med rubrik; innehållet rullar alltid. `error` (`UiError`), `dirty` ("Släng ändringar?" vid bakåt, svep och tryck utanför), `canDismiss`, `hide`, `footer` (knappraden står fast i botten) | Dagbokens parametrar; ReseApotekets rullning gäller också med `footer` |
| `AppButton` | Den enda knappen: Primary, Secondary, Outlined, Text, Destructive; `loading`, `compact`, `contentDescription`. Appens knappstil `AppButtonStyle(minHeight, morph, secondaryTone)` sätts en gång med `LocalAppButtonStyle` (eller parametern `style`); `AppButtonDefaults.MinHeight` 56 dp / `CompactHeight` 40 dp | Fonder: `AppButtonStyle(minHeight = 40.dp, morph = true, secondaryTone = Tone.Primary)`; standard bevarar ReseApoteket och Dagboken |
| `AppCard` | Kortet: `tone`, `contentColor` (standard `null`: ärver omgivande `LocalContentColor`; Dagboken `onSurface`), `onClick` (hela kortet en nod). Appens kortstil `AppCardStyle(border, padding, segmentGap, contentSpacing)` med `LocalAppCardStyle` (eller `style`); ett tonat kort har ingen kontur | ReseApotekets `ApotekCard` heter nu `AppCard`. Fonder: kontur 1 dp `outlineVariant`, `padding = Spacing.l`, `contentSpacing = 0.dp`; Filled/Error är Fonders egna toner |
| `AppDivider` | Avdelare mellan rader i ett kort | – |
| `AppFilterChip` | Filterchip; `icon`, `onLongClick`, `value` (värde efter etiketten), `emphasized` (det aktiva bland flera valda), `pending` (`ChipPending`: väntande läge, tonat med streckad kant, ikon och tilläggstext) | `chipColors()` är publik för egna chips |
| `AppFloatingToolbar` | Toppnivånavigeringen: flytande flikar; `action` (`ToolbarAction`, huvudknapp i `secondary` till höger – Dagboken; med `menu: List<AppMenuItem>` fäller knappen ut en meny och läses "Hopfälld"/"Utfälld" – Fonders plusknapp), `style: ToolbarStyle` och `actionMenu: ActionMenuStyle` (standard ur `AppBrandStyle`; se Sidfoten), `ToolbarItem.badgeCount` (märke med antal i `secondary` på en ovald flik, läses "Beslut, 3 nya" – Fonder). `LocalBottomClearance`, `FloatingToolbarClearance`, `NoBottomClearance` | – |
| `AppHeader` | Kodhäst-sidhuvudet på en flik (se Sidhuvudet): `title`, `scrollBehavior`, `subtitle`, `syncing` (`LocalTopBarSyncing`), `identity` (`LocalAppIdentity`), `status`, `account` (`LocalTopBarAccount`), `actions`, `titleModifier`; taggen `APP_HEADER_TAG` | Ny (#42); ramarnas flikläge med `LocalAppIdentity` |
| `AppIconButton` | Ikonknapp, minst 48 dp, alltid `contentDescription`; `tone` (platta och ikon för Tonal, ikonen för Plain; standard Material-färgerna) | – |
| `AppLoading` | Expressive laddningsindikator mitt på ytan; med `AppBrandStyle(dotLoading = true)` Kodhästs två prickar som hoppar | – |
| `AppMenu` | Menyn bakom ⋮ (`AppMenuItem`: `icon`, `destructive`, `section` (rubrik), `checked` (kryssrad som håller menyn öppen), `supporting`/`supportingTone` (undertext), `muted` (dämpad etikett)) | `section`, `checked` från Dagboken; `asSplitMenu()` från ReseApoteket |
| `AppMenuPopup` | Själva menyn, för egna knappar som öppnar en meny | Publik (Dagboken använder den direkt) |
| `AppPullToRefresh` | Dra för att uppdatera runt eget rullbart innehåll, för skärmar utan ram: `refreshing`, `onRefresh` (`null` = ingen gest); samma yta och tagg (`ENTITY_REFRESH_TAG`) som i ramarna | Fonder: Översikt, Innehav, Beslut |
| `AppSegmentedChoice` | Ett val av få alternativ som segmentknappar | – |
| `AppSnackbarHost` | Den enda platsen där meddelanden visas | – |
| `AppTextField` | Det enda textfältet; `maxLength` (`AppTextFieldDefaults.SHORT_MAX_LENGTH` 200 / `LONG_MAX_LENGTH` 2000), `maxLines`, `suffix`, `valueIsPlaceholder` | Dagboken anger `maxLength = 5000` för långa fält |
| `AppTopBar` | Skärmrubriken, stor eller liten; tillbakapilen `TopBarBackStyle(variant, tone)` via `LocalTopBarBackStyle` (eller parametern `backStyle`), som också ramarna följer | Synkindikatorn är appens: den sätts som `LocalTopBarStatus` (eller parametern `status`). `titleModifier` läggs på rubriken (också via `EntityDetailScreen`). Standardpilen är den tonade plattan; Fonder: `TopBarBackStyle(variant = IconButtonVariant.Plain)` |
| `BrandMark` | Kodhästs märke: hästen (`horseColor`, standard `onSurfaceVariant`) och de två prickarna (`dotsColor`, standard `primary`), dekor utan semantik; storlek med `modifier`, standard `BrandMarkDefaults.Size` 16 dp (profilens minsta) | Ny (android-ui #28) |
| `CardSegment` | Ett segment i ett sammanfogat kort (`top`, `bottom`), `contentColor`, `onClick`, `style`; med kontur delar bitarna skiljelinje | Ersätter Fonders `AppCardGroupPosition` |
| `CheckRow` | Kryssrad med morfande kryss; `onClick` (krysset blir egen knapp), `accent`, `note`, `menu` (långtryck + ⋮, går före bubblan), `below`, `enabled`, `done`/`tinted` (standard `checked`: överstruken och tonad) | Dagbokens `note`, `menu`, `below`, `enabled`; Fonder `done = false, tinted = false` för "ta med" |
| `CollapsibleHeader` | Titelraden i det som fälls ihop: hela raden växlar, minst 48 dp, läses som knapp ("Fäll ut"/"Fäll ihop") med läget som `stateDescription`, chevronen sist fjädrar ett halvt varv. `expandLabels(expanded)` (`ExpandLabels`: `action`, `state`) och `chevronRotation(expanded)` för egna chevronknappar | Var intern (hopfällbara grupper i listramen); ersätter Dagbokens `CollapsibleHeader`, `expandLabels` och `chevronRotation` |
| `ChipRow` | En rad chips som bryts eller rullar | – |
| `ChoiceChips` | Ett val av flera som chips; `onClear` (Dagboken). Överlagring för flerval (`selected: Set<T>`, `onToggle`) | Flervalet är nytt (Dagboken #321) |
| `ChoiceGrid` | Val i ett rutnät med egen cell; `selectionRing` (Dagboken: ring i primärfärg) | – |
| `ColorSwatchPicker` | Färgval; `marker` krävs: `SwatchMarker.RingAndCheck(checkColor)` (ReseApoteket) eller `GridRing` (Dagboken) | `marker` saknar standard – bockens färg är appens |
| `Confetti` | Kort konfettiregn som belöning | `colors` (standard `AppTheme.extendedColors.accents`) |
| `ConfirmDialog` | Bekräftelse; `destructive`, `onDecline`, `dismissLabel` (`null` = bara en knapp), `content` (fält under texten) | Dagbokens `dismissLabel: String?` och `content` |
| `DateField` | Datumfält (kotlinx `LocalDate`, "lör 19 dec 2026"); `emptyLabel`, `onClear` (✕ "Till …"), `context` för TalkBack | Dagbokens parametrar |
| `DeleteConfirmDialog` | Permanent radering av en `DeleteAction` (titel, text, "Radera" i felfärg); titeln t.ex. `R.string.delete_title_format` ("Radera %s?") | Var appens egen (`DeleteAction` från `EntityEditScreen`) |
| `DiscardChangesDialog` | "Släng ändringar?" – "Släng" / "Fortsätt redigera" | Var appens egen |
| `EmojiPicker` | Välj en emoji ur `emojis` med `ChoiceGrid`s ring | Listan krävs – apparnas listor (`PERSON_EMOJIS`, `DEFAULT_EMOJIS`) stannar i appen; ReseApotekets egen markering ersätts av ringen |
| `EmptyState` | Tomt läge med ikon, rubrik, text och knapp; `compact` (Dagboken); `actions: List<EmptyAction>` (en eller flera knappar i full bredd, `label`, `onClick`, `icon`, `variant`); med `AppBrandStyle(emptyStateMark = true)` `BrandMark` i ikonrutan, bara i fullstort läge utan fel | Fonders tomma lägen med flera val |
| `EntityDetailScreen` | Detaljramen: huvud, sektioner, Redigera och meny; underskärm med `title`, flikläge med `onBack = null`; `onRefresh`/`refreshing` | Union av apparnas; se Ramarna |
| `EntityEditScreen` | Redigeringsramen med `EditorState`: Spara, "Släng ändringar?", sparfel, arkivera/återställ/radera, `formError`, tangentbordet | Union av apparnas; se Ramarna |
| `EntityListScreen` | Listramen: laddar, tomt, fel, grupper i kort, kolumner efter bredd, arkivering, `filter`, `footer`, `onRefresh`/`refreshing` | Union av apparnas; se Ramarna |
| `ErrorSnackbar` | Visar ett `UiError` en gång; `template` (t.ex. "Synken nekades: %1$s"), `duration` | Tar `UiError` i stället för appens `DataError` |
| `ExpandableContent` | Det som fälls ut: fjädrar fram (`defaultSpatialSpec`) och fälls snabbt ihop (`fastSpatialSpec`); `visible` | Flyttad från Dagboken (#46) |
| `ExampleChips` | Exempel att trycka in i ett fält | – |
| `Foldout` | Hopfällbar sektion: hela titelraden växlar (`CollapsibleHeader`), `summary` under titeln i stängt läge, `trailing` (egen kontroll före chevronen, växlar inte), `header` (ersätter titeln), `contentSpacing` | Flyttad från Dagboken (#46) |
| `FieldError` | Felraden under ett fält | – |
| `GroupLabel` | Etikett över en grupp | – |
| `InfoPill` | Pill med ton; `trailingIcon`, `role`, `onClickLabel`, `colors`, `shape` (standard `AppTheme.shapes.pill`), `contentDescription` | Fonder: `shape = AppTheme.shapes.smallTile`, egna toner för Tertiary/Neutral |
| `ItemRow` | Den enda listraden: `leading`, `trailing`, `tinted`, `done`, `accent`, `navigates`, `inactive`, `below`, `titleHighlight`, `icon`, `belowAlignsWithTitle`, `overline` (ovanför titeln), `titleStyle` (standard `itemTitle`). Kapad titel: långtryck visar hela titeln och undertexten i en bubbla | Dagbokens `icon`, `belowAlignsWithTitle`; bubblan nu även i Dagboken. Avståndet titel–undertext: `LocalItemRowTextGap` (standard 2 dp; Dagboken `Spacing.xs`) |
| `ItemRowText` | Listradens titel och undertext, för appens egna kort | Dagbokens `RowText` |
| `LabeledGroup` | Etikett, hjälptext och innehåll; `helperAbove` (Dagboken) | – |
| `LeadingSlot` | 48 dp-ruta för det ledande elementet, så att titlarna linjerar | – |
| `LoadErrorState` | Läsfel med "Försök igen" (bara om `retryable`) | Tar `UiError` |
| `MakerMark` | Kodhästs signatur: `BrandMark` och "En kodhäst-app" (`text`, sträng `maker_mark`) i `caption` och `onSurfaceVariant`, en nod som läses som texten | Ny (android-ui #28) |
| `MessageSnackbar` | Visar en text en gång i `AppSnackbarHost` | – |
| `MorphingCheck` | Krysset som morfar från ring till kaka med bock (bara visning) | – |
| `NoteIndicator` | Anteckningsikon som visar anteckningen i en dialog | – |
| `PausableRow` | Rad som kan pausas: typ som pill, nedtonad "Pausat"; `pillTone` krävs | Tonen är appens (ReseApoteket `Tone.Blue`, Dagboken `Tone.Neutral`) |
| `PickerField` | Fält som öppnar en väljare; `emptyLabel`, `contentLabel` | – |
| `QuantityStepper` | − värde +; `step`, `valueText`, `buttonSize` (standard `LocalQuantityStepperButtonSize`: `QuantityStepperDefaults.ButtonSize` 44 dp, `TouchTargetButtonSize` 48 dp) | Dagboken sätter `TouchTargetButtonSize` en gång i sitt tema |
| `SectionHeader` | Sektionsrubrik med antal; `countTone`; laddningsindikator med `working`; med `AppBrandStyle(sectionDots = true)` två prickar i `primary` före titeln när ikonrutan saknas | – |
| `SectionFoldout` | `Foldout` med `SectionHeader` i titelraden: `icon`, `count`, `tone`, `countTone`, `trailing` (t.ex. en tonad lägg till-knapp, `AppIconButton(…, variant = IconButtonVariant.Tonal)`) | Flyttad från Dagboken (#46); `trailing` är ny (ReseApotekets persondetalj) |
| `SignInButton` | Utloggat läge i sidhuvudet: Googles "G" i sina egna färger och "Logga in" (`text`), konturerad i rubrikradens lägre höjd; `loading` | Ny (#42) |
| `SuggestionField` | Textfält med förslag (`Suggestion`) i ett kort under, antalet läses upp | – |
| `SwipeBackground` | Det som syns bakom en svept rad: `icon` och `label` i högerkanten, `colors` (`ToneColors`, standard primärtonen), `shape` (standard `row`) | Ersätter bakgrunden i Dagbokens `SwipeToDelete` (`ic_delete`, felfärgen, `card`) |
| `SwipeToHide` | Arkivera/dölj med svep åt vänster, och samma åtgärd för TalkBack; bakgrunden är `SwipeBackground` med `icon` och `colors` (standard arkivera i primärtonen) | – |
| `SwitchControl` | Själva växeln, för appens kort med reglage; `enabled` | – |
| `SwitchRow` | Inställningsrad med växel; `onClick` (växeln blir egen kontroll), `inactive` (tonar ned texten, inte växeln), `enabled` (låser växeln) | Dagbokens `inactive`; Fonders `enabled` |
| `TimeField` | Tidsfält (kotlinx `LocalTime`) som öppnar `TimePickerPopup` | – |
| `TimePickerPopup` | Tidsväljaren som dialog | – |
| `UndoSnackbar` | "Arkiverad – Ångra" för `UndoRequest`; `format`, `onRequestDismissed` | – |

**Modifierare:** `Modifier.longPress(label, onLongPress, haptic = false, semantics = LongPressSemantics.LongClick)` –
långtryck ovanpå en yta vars klick ägs av något annat (`AppFilterChip`, Dagbokens känslor): avbryts av
rörelse, tar resten av gesten efter långtrycket, `haptic` ger en puls, TalkBack får långtrycksåtgärden
(`LongClick`) eller en anpassad åtgärd (`CustomAction`); `onLongPress = null` gör ingenting.

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

Versionen står i `gradle.properties` (`version=`). `.github/workflows/publish.yml` publicerar alla fyra
artefakterna till GitHub Packages, antingen när en tagg `v<version>` pushas eller manuellt
(Actions → Publicera → Run workflow på `main`), som då skapar taggen själv. Molnsessioner kan inte
pusha taggar och använder därför den manuella vägen.

## Versionshistorik

| Version | Ändring |
|---|---|
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
