var SM = window.SM || {};

SM.API_BASE = "/api/v1/ezyclub";
SM.settings = SM.settings || {};
SM.COVER_COLORS = [
    "#0f766e", "#2563eb", "#7c3aed", "#db2777", "#ea580c",
    "#16a34a", "#0891b2", "#4f46e5", "#be123c", "#a16207"
];

SM.t = function (key, fallback) {
    var messages = (typeof ezyweb !== "undefined" && ezyweb.messages) || {};
    return messages[key] || fallback || key;
};

SM.format = function (template, values) {
    return String(template).replace(/%(\w+)%/g, function (match, name) {
        return values && values[name] != null ? values[name] : match;
    });
};

SM.escape = function (value) {
    return ezyweb.escapeHtml(value == null ? "" : String(value));
};

SM.icon = function (name, extraClass) {
    return '<svg class="icon ' + (extraClass || "") + '"><use href="#i-' + name + '"></use></svg>';
};

SM.url = function (path) {
    if (typeof ezyweb !== "undefined" && ezyweb.lang && ezyweb.appendLangParameterToUrl) {
        return ezyweb.appendLangParameterToUrl(path);
    }
    return path;
};

SM.colorOf = function (text, preferred) {
    if (preferred && /^#[0-9a-fA-F]{6}$/.test(preferred)) {
        return preferred;
    }
    var hash = 0;
    var value = String(text || "");
    for (var i = 0; i < value.length; ++i) {
        hash = (hash * 31 + value.charCodeAt(i)) | 0;
    }
    return SM.COVER_COLORS[Math.abs(hash) % SM.COVER_COLORS.length];
};

SM.coverStyle = function (text, preferred) {
    var color = SM.colorOf(text, preferred);
    return "background: linear-gradient(135deg, " + color + ", color-mix(in srgb, " + color + " 55%, #0f172a));";
};

SM.initialOf = function (text) {
    var value = String(text || "?").trim();
    return value ? value.charAt(0).toUpperCase() : "?";
};

SM.avatar = function (user, sizeClass, showLevel) {
    user = user || {};
    var name = user.displayName || user.username || "?";
    var inner = user.avatarUrl
        ? '<img src="' + SM.escape(user.avatarUrl) + '" alt="' + SM.escape(name) + '">'
        : SM.escape(SM.initialOf(name));
    var level = showLevel !== false && user.level
        ? '<span class="avatar-level">' + SM.escape(String(user.level)) + "</span>"
        : "";
    return '<span class="avatar ' + (sizeClass || "") + '" style="background:' +
        SM.colorOf(user.uuid || name) + '">' + inner + level + "</span>";
};

SM.timeAgo = function (timestamp) {
    if (!timestamp) {
        return "";
    }
    var seconds = Math.max(0, Math.floor((Date.now() - timestamp) / 1000));
    if (seconds < 60) {
        return SM.t("just_now", "just now");
    }
    var minutes = Math.floor(seconds / 60);
    if (minutes < 60) {
        return SM.format(SM.t("minutes_ago", "%value%m"), {value: minutes});
    }
    var hours = Math.floor(minutes / 60);
    if (hours < 24) {
        return SM.format(SM.t("hours_ago", "%value%h"), {value: hours});
    }
    var days = Math.floor(hours / 24);
    if (days < 30) {
        return SM.format(SM.t("days_ago", "%value%d"), {value: days});
    }
    return new Date(timestamp).toLocaleDateString(SM.locale());
};

SM.locale = function () {
    return (typeof ezyweb !== "undefined" && ezyweb.lang) || document.documentElement.lang || undefined;
};

SM.formatDateTime = function (timestamp) {
    return new Date(timestamp).toLocaleString(SM.locale(), {
        weekday: "short",
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
};

SM.formatNumber = function (value) {
    return Number(value || 0).toLocaleString(SM.locale());
};

SM.toast = function (message, type) {
    var container = document.getElementById("toastContainer");
    if (!container) {
        container = document.createElement("div");
        container.id = "toastContainer";
        container.className = "toast-container";
        document.body.appendChild(container);
    }
    var toast = document.createElement("div");
    toast.className = "toast toast-" + (type || "success");
    toast.innerHTML = SM.icon(type === "error" ? "alert" : "check") + "<span>" + SM.escape(message) + "</span>";
    container.appendChild(toast);
    setTimeout(function () {
        toast.remove();
    }, 3200);
};

SM.rememberLoginCallbackUrl = function () {
    try {
        sessionStorage.setItem("loginCallbackUrl", location.pathname + location.search);
    } catch (e) {
    }
};

SM.openLoginModal = function () {
    if (typeof showLoginModal === "function") {
        showLoginModal();
        return false;
    }
    location.href = SM.url("/login");
    return false;
};

SM.openRegisterModal = function () {
    if (typeof showRegisterModal === "function") {
        showRegisterModal();
        return false;
    }
    location.href = SM.url("/register");
    return false;
};

SM.redirectToLogin = function () {
    SM.rememberLoginCallbackUrl();
    SM.openLoginModal();
};

SM.isLoggedIn = function () {
    return !!SM.loggedIn;
};

SM.requireLogin = function () {
    if (!SM.isLoggedIn()) {
        SM.redirectToLogin();
        return false;
    }
    return true;
};

SM.api = function (method, path, data) {
    return new Promise(function (resolve, reject) {
        $.ajax({
            type: method,
            url: SM.API_BASE + path,
            contentType: "application/json",
            data: data === undefined ? undefined : JSON.stringify(data),
            success: function (response) {
                resolve(response);
            },
            error: function (xhr) {
                if (xhr.status === 401) {
                    SM.redirectToLogin();
                } else if (xhr.status === 403) {
                    SM.toast(SM.t("permission_denied", "You do not have permission to do this"), "error");
                } else if (xhr.status !== 400) {
                    SM.toast(SM.t("something_went_wrong", "Something went wrong, please try again"), "error");
                }
                reject(xhr);
            }
        });
    });
};

SM.get = function (path) {
    return SM.api("GET", path);
};

SM.post = function (path, data) {
    return SM.api("POST", path, data === undefined ? {} : data);
};

SM.put = function (path, data) {
    return SM.api("PUT", path, data === undefined ? {} : data);
};

SM.del = function (path) {
    return SM.api("DELETE", path);
};

SM.showFormErrors = function (form, xhr) {
    $(form).find(".form-error").remove();
    var errors = (xhr && xhr.responseJSON) || {};
    Object.keys(errors).forEach(function (field) {
        var input = form.querySelector('[name="' + field + '"]');
        var message = SM.t("error_" + errors[field], errors[field]);
        var error = document.createElement("div");
        error.className = "form-error";
        error.textContent = message;
        if (input) {
            input.closest(".form-group").appendChild(error);
        } else {
            form.prepend(error);
        }
    });
};

SM.openModal = function (html, sizeClass) {
    SM.closeModal();
    var backdrop = document.createElement("div");
    backdrop.className = "modal-backdrop";
    backdrop.id = "smModal";
    backdrop.innerHTML = '<div class="modal-dialog ' + (sizeClass || "") + '">' + html + "</div>";
    backdrop.addEventListener("mousedown", function (event) {
        if (event.target === backdrop) {
            SM.closeModal();
        }
    });
    document.body.appendChild(backdrop);
    document.body.style.overflow = "hidden";
    return backdrop;
};

SM.closeModal = function () {
    var modal = document.getElementById("smModal");
    if (modal) {
        modal.remove();
        document.body.style.overflow = "";
        if (SM.onModalClosed) {
            var callback = SM.onModalClosed;
            SM.onModalClosed = null;
            callback();
        }
    }
};

SM.confirm = function (message, onConfirm) {
    SM.openModal(`
        <div class="modal-body">
            <p>${SM.escape(message)}</p>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-outline" onclick="SM.closeModal()">${SM.escape(SM.t("cancel", "Cancel"))}</button>
            <button type="button" class="btn btn-danger" id="smConfirmButton">${SM.escape(SM.t("confirm", "Confirm"))}</button>
        </div>
        `);
    document.getElementById("smConfirmButton").addEventListener("click", function () {
        SM.closeModal();
        onConfirm();
    });
};

SM.loadingHtml = function () {
    return '<div class="loading-row"><span class="spinner"></span></div>';
};

SM.emptyHtml = function (icon, message) {
    return '<div class="empty-state">' + SM.icon(icon) + "<p>" + SM.escape(message) + "</p></div>";
};

SM.toggleLangMenu = function (event) {
    event.stopPropagation();
    document.getElementById("langSwitcher").classList.toggle("open");
};

SM.changeLanguage = function (lang) {
    if (typeof ezyweb !== "undefined" && ezyweb.onLanguageSelected) {
        ezyweb.onLanguageSelected(lang);
        return;
    }
    var url = new URL(location.href);
    url.searchParams.set("lang", lang);
    location.href = url.toString();
};

SM.toggleUserMenu = function (event) {
    event.stopPropagation();
    document.getElementById("userMenu").classList.toggle("open");
};

SM.closeAnnouncement = function () {
    var bar = document.getElementById("announcementBar");
    if (bar) {
        bar.classList.add("d-none");
    }
    try {
        sessionStorage.setItem("ezyclub.announcementClosed", "1");
    } catch (e) {
    }
};

SM.onHeaderSearch = function (event, input) {
    if (event.key === "Enter") {
        location.href = SM.url("/?keyword=" + encodeURIComponent(input.value.trim()));
    }
};

SM.loadMyCommunities = function () {
    var container = document.getElementById("myCommunities");
    if (!container || !SM.isLoggedIn()) {
        return;
    }
    SM.get("/my-communities").then(function (communities) {
        container.innerHTML = communities.map(function (community) {
            return `
                <a href="${SM.escape(SM.url("/c/" + community.slug))}">
                    ${community.logoUrl
                        ? '<span class="community-logo avatar-sm"><img src="' + SM.escape(community.logoUrl) + '" alt=""></span>'
                        : '<span class="community-logo avatar-sm" style="' + SM.coverStyle(community.slug, community.themeColor) + '">' + SM.escape(SM.initialOf(community.name)) + '</span>'}
                    <span>${SM.escape(community.name)}</span>
                </a>
                `;
        }).join("");
    });
};

$(document).ready(function () {
    var announcement = document.getElementById("announcementBar");
    try {
        if (announcement && !sessionStorage.getItem("ezyclub.announcementClosed")) {
            announcement.classList.remove("d-none");
        }
    } catch (e) {
    }
    document.addEventListener("click", function () {
        $("#langSwitcher, #userMenu").removeClass("open");
    });
    SM.loadMyCommunities();
});
