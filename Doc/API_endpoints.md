# Project & Issue Tracking System - API Endpoints

This document outlines all the REST API endpoints available in the backend system, their HTTP methods, and their intended usage.

All API routes (except registration and login) require a **Bearer Token** generated from `/auth/login` to be passed in the `Authorization` header.

---

## 🔐 1. Authentication Endpoints

| Method | Endpoint | Description | Access / Role |
| :--- | :--- | :--- | :--- |
| `POST` | `/auth/login` | Authenticate using `email` and `password` to receive a JWT access token. | Public |

---

## 👤 2. User Management Endpoints

| Method | Endpoint | Description | Access / Role |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/users` | Register a new user account. | Public |
| `GET` | `/api/users/me` | Fetch the profile data of the currently authenticated user. | Any Authenticated |
| `PUT` | `/api/users/me` | Update the currently authenticated user's profile details. | Any Authenticated |
| `DELETE` | `/api/users/me` | Deactivate the currently authenticated user's account. | Any Authenticated |
| `GET` | `/api/users/public/{id}` | Fetch limited, public profile data for a specific user ID. | Any Authenticated |
| `GET` | `/api/users/search?query=...` | Search for active members by name or email query. | Any Authenticated |
| `GET` | `/api/users/admin/all` | Fetch a full list of all registered platform users. | **Admin Only** |
| `DELETE` | `/api/users/admin/{id}` | Permanently delete or deactivate a specific user by ID. | **Admin Only** |

---

## 📂 3. Project Management Endpoints

| Method | Endpoint | Description | Access / Role |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/projects` | Create a new project. The creator automatically becomes the Project Leader. | Any Authenticated |
| `GET` | `/api/projects` | List projects. Admins see all projects; normal users only see projects they lead or are assigned to. | Any Authenticated |
| `GET` | `/api/projects/{id}` | Fetch detailed data for a specific project. | Project Member or Admin |
| `PUT` | `/api/projects/{id}` | Update project details (Name, Description, Key, Active Status). | Project Leader or Admin |
| `DELETE` | `/api/projects/{id}` | Delete a project and cascade delete all its associated issues. | Project Leader or Admin |
| `POST` | `/api/projects/{id}/members/{userId}` | Add an existing user to a project as a member. | Project Leader or Admin |
| `DELETE`| `/api/projects/{id}/members/{userId}` | Revoke a member's access from a project. | Project Leader or Admin |

---

## 🎫 4. Issue Tracking Endpoints

*Note: All endpoints below are prefixed with `/api/projects/{projectId}`.*

| Method | Endpoint | Description | Access / Role |
| :--- | :--- | :--- | :--- |
| `POST` | `/issues` | Create a new issue (Task, Bug, Story, Improvement) under the specified project. | Project Member |
| `GET` | `/issues` | List all issues for the project. Supports query filtering: `?status=OPEN` or `?assigneeId=...`. | Project Member |
| `GET` | `/issues/{id}` | Fetch the comprehensive details, descriptions, and workflow states for a specific issue. | Project Member |
| `PUT` | `/issues/{id}` | Fully update an issue (Title, Description, Priority, Issue Type, Assignee). | Project Member |
| `DELETE` | `/issues/{id}` | Permanently delete a specific issue. | **Admin Only** |
| `PATCH` | `/issues/{id}/status` | Transition the issue status (e.g. `OPEN` → `IN_PROGRESS` → `DONE`). | Project Member |
| `PATCH` | `/issues/{id}/assign?assigneeId=...`| Re-assign the issue to a specific project member. | **Admin Only** |
