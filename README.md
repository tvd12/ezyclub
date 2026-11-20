# Ezyclub

Ezyclub is an online community platform built on [EzyPlatform](https://ezyplatform.com): people discover and join communities, post and comment in a feed, take courses in a classroom, RSVP to events on a calendar and climb a leaderboard by earning points.

![Discover communities](docs/images/discover.png)

## Features

### Discover and join communities

Search communities by keyword, filter them by category, then join with one click. Anyone can create their own community with a name, URL, description, category, cover image and theme color.

| Discover | Create a community |
|---|---|
| ![Discover](docs/images/discover.png) | ![Create a community](docs/images/create-community.png) |

### Sign in and register

Login and register modals in the Ezyclub brand, with social login and email activation provided by EzyLogin.

| Sign in | Register |
|---|---|
| ![Sign in](docs/images/login.png) | ![Register](docs/images/register.png) |

### Community feed

Members write posts in categories, like posts, comment and reply. Posts can be pinned and filtered by category.

| Feed | New post |
|---|---|
| ![Community feed](docs/images/community-feed.png) | ![New post](docs/images/create-post.png) |

| Post and comments | About |
|---|---|
| ![Post detail](docs/images/post-detail.png) | ![About](docs/images/about.png) |

### Classroom

Courses made of lessons with text and video. Members mark lessons as complete and see their progress per course. A course can be unlocked at a member level.

| Courses | Lesson |
|---|---|
| ![Classroom](docs/images/classroom.png) | ![Lesson](docs/images/lesson.png) |

### Calendar and events

Monthly calendar with upcoming events, online meeting links and RSVPs.

| Calendar | Event |
|---|---|
| ![Calendar](docs/images/calendar.png) | ![Event](docs/images/event.png) |

### Members and leaderboards

Members earn points when their posts and comments are liked, level up from 1 to 9 and climb the leaderboard.

| Members | Leaderboards |
|---|---|
| ![Members](docs/images/members.png) | ![Leaderboards](docs/images/leaderboards.png) |

### Community settings

Owners and admins edit the community details, cover, logo, privacy and post categories.

![Community settings](docs/images/community-settings.png)

### Mobile

<p>
    <img src="docs/images/mobile-discover.png" alt="Mobile discover" width="280">
    <img src="docs/images/mobile-feed.png" alt="Mobile feed" width="280">
</p>

### Admin

The admin plugin adds a dashboard with community statistics, a community list where admins can activate or deactivate communities, and settings such as the primary color, page sizes, whether users can create communities, the language switcher and the announcement bar.

| Dashboard | Communities |
|---|---|
| ![Admin dashboard](docs/images/admin-dashboard.png) | ![Admin communities](docs/images/admin-communities.png) |

![Admin settings](docs/images/admin-settings.png)

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
