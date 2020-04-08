package com.technion.vedibarta.dagger

import com.technion.vedibarta.utilities.DataBase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataBaseModule
{
    @Provides
    @Singleton
    fun database(): DataBase = DataBase()
}