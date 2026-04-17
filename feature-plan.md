# Additional Feature Plan: Event Comments

## Selected feature

Comments for events.

## Why this feature

Comments are realistic for the ExploreWithMe domain: users can discuss events, ask questions, and leave short feedback after participation. The feature fits naturally into the main service without changing the statistics service contract.

## Implementation plan

1. Add comment entity with id, text, author, event, status, created and updated timestamps.
2. Add DTOs for creating, updating, moderating and returning comments.
3. Add private API for users to create, update and delete their own comments.
4. Add public API to list approved comments for an event.
5. Add admin API to moderate comments and reject inappropriate content.
6. Validate comment text length and ownership rules.
7. Allow comments only for published events.
8. Add repository queries with pagination and filtering by event, author and status.
9. Cover service rules with unit tests and repository/controller integration tests.
