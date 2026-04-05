package uk.co.zlurgg.thedayto.app.di

import uk.co.zlurgg.thedayto.auth.di.authModule
import uk.co.zlurgg.thedayto.core.di.coreModule
import uk.co.zlurgg.thedayto.di.debugModules
import uk.co.zlurgg.thedayto.journal.di.journalModule
import uk.co.zlurgg.thedayto.journal.di.journalViewModelModule
import uk.co.zlurgg.thedayto.notification.di.notificationModule
import uk.co.zlurgg.thedayto.sync.di.syncModule

val appModule = listOf(
    coreModule,
    authModule,
    journalModule,
    journalViewModelModule,
    notificationModule
) + debugModules + listOf( // debugModules provides Firestore (emulator or production)
    syncModule
)
