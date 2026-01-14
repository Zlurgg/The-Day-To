package uk.co.zlurgg.thedayto.di

import org.koin.core.module.Module

/**
 * Empty module list for release builds.
 * Debug-only modules are not included in production.
 */
val debugModules: List<Module> = emptyList()
