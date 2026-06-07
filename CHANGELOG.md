# Changelog

Format: Keep a Changelog; semver.

## [Unreleased]

## [1.0.0] - 2026-06-07
First public release.

### Added
- `Wire` client with Bearer auth, automatic idempotency keys, retry/backoff with jitter
  honoring `Retry-After`, and configurable `WireOptions`.
- `paymentIntents`, `charges`, `events`, `webhookEndpoints` resources.
- Lazy auto-pagination via `AutoPagingIterable` following `has_more`.
- Webhook signature verification (`Webhooks.verify` / `verifyAt`) with constant-time compare.
- Typed `WireException` decoded from the error envelope; distinct `WireConnectionException`
  for network/timeout failures.
