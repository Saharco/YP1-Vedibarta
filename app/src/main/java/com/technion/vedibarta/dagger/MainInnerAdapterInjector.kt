package com.technion.vedibarta.dagger

import com.technion.vedibarta.main.MainFireBaseAdapter
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [MainInnerAdapterModule::class])
interface MainInnerAdapterInjector
{
    fun inject(target: MainFireBaseAdapter)
}