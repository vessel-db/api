/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.mast.app.instance

enum class InstancePermission {

    POWER_START, POWER_STOP, POWER_RESTART,
    CONSOLE_READ, CONSOLE_SEND,
    CONFIG_READ, CONFIG_UPDATE,
    BACKUP_CREATE, BACKUP_READ, BACKUP_DELETE, BACKUP_DOWNLOAD, BACKUP_RESTORE,

}