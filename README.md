# Ezyclub

Ezyclub is an online community platform built on [EzyPlatform](https://ezyplatform.com): people discover and join communities, post and comment in a feed, take courses in a classroom, RSVP to events on a calendar and climb a leaderboard by earning points.

## Modules

| Module | Purpose |
|---|---|
| `ezyclub-sdk` | Shared entities, repositories, models and services |
| `ezyclub-web-plugin` | Public REST APIs under `/api/v1/ezyclub` |
| `ezyclub-theme` | Thymeleaf theme: discover page, community feed, classroom, calendar, members, leaderboards, about, settings, create community |
| `ezyclub-admin-plugin` | Admin dashboard, community management and settings |

Ezyclub reuses the tables of EzyPlatform and its plugins (`ezyarticle`, `ezycrm`, `ezyrating`, `ezymail`, `ezylogin`), so it does not need its own tables.

## Requirements

- Java 8+, Maven
- EzyPlatform with the plugins `ezyarticle`, `ezycrm`, `ezyrating`, `ezymail` and `ezylogin` installed
- `EZYPLATFORM_HOME` pointing to your EzyPlatform installation

## Run locally

Run the startup test classes from your IDE:

- Web (theme + web plugin, port 8080): `ezyclub-theme/src/test/java/.../EzyclubThemeStartupTest`
- Admin (port 9090): `ezyclub-admin-plugin/src/test/java/.../EzyclubAdminPluginStartupTest`

Copy `ezyclub-theme/src/main/resources/messages/*.properties` to `$EZYPLATFORM_HOME/web/themes/ezyclub/resources/messages/` so the theme's translations are loaded.

## Build and export

```bash
./export.sh
```

## Languages

English and Vietnamese (`?lang=en`, `?lang=vi`).

## Development

See `AGENTS.md` for the project layout and conventions and `SKILL.md` for how to add a feature through the EzyPlatform layers.
