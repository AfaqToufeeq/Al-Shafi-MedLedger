package com.pentabytex.alshafimedledger.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.pentabytex.alshafimedledger.utils.Constants.SharedPrefKeys.APP_NAME
import com.pentabytex.alshafimedledger.utils.CoroutineDispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideCoroutineDispatcherProvider(): CoroutineDispatcherProvider {
        return CoroutineDispatcherProvider()
    }

    @Provides
    @Singleton
    fun provideSharedPreferencesManager(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application.applicationContext}

}