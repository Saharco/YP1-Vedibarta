package com.technion.vedibarta.dagger

import com.technion.vedibarta.login.LoginActivity
import com.technion.vedibarta.utilities.VedibartaActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DataBaseModule::class])
interface DataBaseInjector
{
    companion object
    {
        val injector = DaggerDataBaseInjector.builder()
                .dataBaseModule(DataBaseModule())
                .build()
    }

    fun inject(target: VedibartaActivity)

    fun inject(target: LoginActivity)
}