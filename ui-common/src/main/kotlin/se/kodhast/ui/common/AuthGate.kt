package se.kodhast.ui.common

/**
 * Var appen står i inloggningen – vad `AuthGateHost` visar. Appens `AuthViewModel` (Firebase, konto,
 * hushåll) räknar fram läget; biblioteket har ingen Firebase.
 *
 * - [Loading]: inloggningen kontrolleras vid start (laddning).
 * - [SignedOut]: ingen är inloggad (inloggningen).
 * - [NeedsAccount]: inloggad, men appens konto (hushåll, användare) är inte klart – inloggningen visas
 *   medan appen förbereder det.
 * - [UpdateRequired]: datan är sparad med en nyare app; inget visas eller ändras förrän appen uppdaterats.
 * - [Ready]: appens innehåll.
 */
enum class AuthGate { Loading, SignedOut, NeedsAccount, UpdateRequired, Ready }
