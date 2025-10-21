package com.racoonsfinds.backend.shared.const_;

/**
 * Representa los diferentes estados posibles del usuario durante el proceso
 * de autenticación, verificación y acceso al sistema.
 *
 * Cada estado se usa para indicar al cliente (frontend o API consumer)
 * cuál es la situación actual del usuario.
 */
public final class UserStatus {

    // =====================
    // Estados básicos
    // =====================

    /** Usuario no encontrado en la base de datos. */
    public static final int NOT_FOUND = 0;

    /** Usuario encontrado, pero sin autenticación válida (credenciales incorrectas o sesión caducada). */
    public static final int NOT_AUTH = 1;

    /** Usuario autenticado correctamente y con acceso autorizado. */
    public static final int AUTH_SUCCESS = 2;


    // =====================
    // Bloqueos
    // =====================

    /** Usuario bloqueado temporalmente (por intentos fallidos, sanciones, etc.). */
    public static final int BLOCKED_TEMP = 3;

    /** Usuario bloqueado permanentemente (por infracción o decisión administrativa). */
    public static final int BLOCKED_PERM = 4;


    // =====================
    // Verificación de cuenta
    // =====================

    /** Usuario aún no ha verificado su cuenta (debe validar código o email). */
    public static final int NOT_VERIFIED = 5;

    /** El código de verificación ha expirado (debe solicitar uno nuevo). */
    public static final int CODE_EXPIRED = 6;

    /** El código ingresado no coincide con el registrado (código inválido). */
    public static final int CODE_INVALID = 7;

    /** No hay un código pendiente de verificación (no fue solicitado). */
    public static final int CODE_NOT_REQUESTED = 8;


    // =====================
    // Constructor privado
    // =====================

    /** Evita la instanciación de esta clase de constantes. */
    private UserStatus() {}
}
