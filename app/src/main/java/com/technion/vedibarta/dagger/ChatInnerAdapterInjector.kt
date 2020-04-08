package com.technion.vedibarta.dagger

import com.technion.vedibarta.chatRoom.ChatRoomAdapter
import com.technion.vedibarta.chatRoom.ChatRoomFireBaseAdapter
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ChatInnerAdapterModule::class])
interface ChatInnerAdapterInjector
{
    fun inject(target: ChatRoomFireBaseAdapter)
}