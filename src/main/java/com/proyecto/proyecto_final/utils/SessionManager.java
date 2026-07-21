package com.heladeria.utils;

import com.heladeria.models.Usuario;

public class SessionManager {
    private static SessionManager instance;
    private Usuario usuarioActual;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    public void logout() {
        this.usuarioActual = null;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public boolean isLoggedIn() {
        return usuarioActual != null;
    }

    public boolean isAdmin() {
        return isLoggedIn() && "admin".equals(usuarioActual.getRol());
    }

    public boolean isVendedor() {
        return isLoggedIn() && "vendedor".equals(usuarioActual.getRol());
    }

    public boolean isCliente() {
        return isLoggedIn() && "cliente".equals(usuarioActual.getRol());
    }
}
