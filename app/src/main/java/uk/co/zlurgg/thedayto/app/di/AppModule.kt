package uk.co.zlurgg.thedayto.app.di

import uk.co.zlurgg.thedayto.auth.di.authModule
import uk.co.zlurgg.thedayto.core.di.coreModule
import uk.co.zlurgg.thedayto.journal.di.journalModule
import uk.co.zlurgg.thedayto.journal.di.journalViewModelModule
import uk.co.zlurgg.thedayto.update.di.updateModule

val appModule = listOf(
    coreModule,
    updateModule,
    authModule,
    journalModule,
    journalViewModelModule
)
