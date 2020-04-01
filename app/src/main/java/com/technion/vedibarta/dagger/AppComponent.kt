package com.technion.vedibarta.dagger

import com.technion.vedibarta.login.LoginActivity
import com.technion.vedibarta.utilities.VedibartaActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent
{
    companion object
    {
        val injector = DaggerAppComponent.builder()
                .appModule(AppModule())
                .build()
    }

    fun inject(target: VedibartaActivity)

    fun inject(target: LoginActivity)
}