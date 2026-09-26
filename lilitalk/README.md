# Lilitalk

Lilitalk is a chat application built around users, chat rooms, chat room members, and messages.

## ERD

```mermaid
erDiagram
    CHAT_ROOMS {
        BIGINT id PK
        VARCHAR name
        TEXT description
        VARCHAR type
        VARCHAR image_url
        BOOLEAN is_active
        INT max_members
        BIGINT created_by FK
        DATETIME created_at
        DATETIME updated_at
    }

    CHAT_ROOM_MEMBERS {
        BIGINT id PK
        BIGINT chat_room_id FK
        BIGINT user_id FK
        VARCHAR role
        BOOLEAN is_active
        BIGINT last_read_message_id
        DATETIME joined_at
        DATETIME left_at
        DATETIME created_at
    }

    MESSAGES {
        BIGINT id PK
        BIGINT chat_room_id FK
        BIGINT sender_id FK
        VARCHAR type
        TEXT content
        BOOLEAN is_edited
        BOOLEAN is_deleted
        BIGINT sequence_number
        DATETIME created_at
        DATETIME edited_at
    }

    APP_USERS ||--o{ CHAT_ROOMS: creates
    APP_USERS ||--o{ CHAT_ROOM_MEMBERS: joins
    CHAT_ROOMS ||--o{ CHAT_ROOM_MEMBERS: has
    APP_USERS ||--o{ MESSAGES: sends
    CHAT_ROOMS ||--o{ MESSAGES: contains
```

## Entity Overview

### app_users

Stores user account information.

- `username` must be unique.
- Stores display name, profile image URL, status message, and active status.
- Tracks the user's last seen time, creation time, and update time.

### chat_rooms

Stores chat room information.

- Stores the room name, description, room type, image URL, and maximum member count.
- References the user who created the chat room through `created_by`.

### chat_room_members

Stores the relationship between users and chat rooms.

- A user can only be registered once in the same chat room.
- Stores member role, active status, last read message ID, joined time, and left time.

### messages

Stores chat messages.

- Each message belongs to a chat room and has a sender.
- Stores message type, content, edit/delete status, and sequence number within the chat room.