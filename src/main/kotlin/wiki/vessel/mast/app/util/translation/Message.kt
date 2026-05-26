/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.mast.app.util.translation

enum class Message(val key: String) {
    UNAUTHORIZED("unauthorized"),
    NOT_FOUND("not_found"),
    FORBIDDEN("forbidden"),
    CONFLICT("conflict"),
    BAD_REQUEST("bad_request"),
    RATE_LIMITED("rate_limited"),
    INTERNAL_ERROR("internal_error")
}