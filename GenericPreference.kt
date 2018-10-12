package com.xspero.xspero

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Qualifier

// separate one shared pref from another

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class User

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class AppSettings


// different files for different shared prefs usages
class PrefsConfig {
    companion object {
        val USER = "user"
        val APP_SETTINGS = "app_settings"
    }
}

//Used in App Module
@Module
class PreferenceModule {

    // user qualified shared prefs
    @Provides
    @User
    internal fun providesUserSettingsPrefs(context: Context): SharedPrefUtils {
        return SharedPrefUtils(context, context.getSharedPreferences(PrefsConfig.USER, Context.MODE_PRIVATE))
    }

    // user settings qualified shared prefs
    @Provides
    @AppSettings
    internal fun providesAppSettingsPrefs(context: Context): SharedPrefUtils {
        return SharedPrefUtils(context, context.getSharedPreferences(PrefsConfig.APP_SETTINGS, Context.MODE_PRIVATE))
    }
}


/**
 * The pref name is used by extended classes to provided a unique name for storage like users.pref, settings.pref etc
 * Used in conjunction with dagger they can be provided by a shared pref module where needed
 */
class SharedPrefUtils @Inject constructor(context: Context, sharedPrefs: SharedPreferences) {

    private val sharedPref: SharedPreferences = sharedPrefs

    /**
     * Returns a generic type based on key
     * If no class is found for that type, it tries to return the type from GSon (maybe we stored an custom object)
     * If everything fails, it returns the default value
     *
     * @param key The key of the shared preferance
     * @param defaultValue The default value for the type <T> provided
     *
     * @return Either the found value or the default value
     */
    inline fun <reified T> SharedPreferences.get(key: String, defaultValue: T): T {
        when (T::class) {
            Boolean::class -> return this.getBoolean(key, defaultValue as Boolean) as T
            Float::class -> this.getFloat(key, defaultValue as Float) as T
            Int::class -> this.getInt(key, defaultValue as Int) as T
            Long::class -> this.getLong(key, defaultValue as Long) as T
            String::class -> this.getString(key, defaultValue as String) as T
            Set::class -> this.getStringSet(key, defaultValue as Set<String>) as T
            // fallback to gson
            else -> {
                Gson().fromJson(key, T::class.java) as T
            }
        }

        return defaultValue
    }


    /**
     * Stores a key-value generic type in shared prefs
     * If no class type is found the generic type will fall back to a gson custom object
     *
     * @param key The key of the shared preferance
     * @param value The value of the shared pref
     */
    inline fun <reified T> SharedPreferences.put(key: String, value: T) {
        val editor = this.edit()

        when (T::class) {
            Boolean::class -> editor.putBoolean(key, value as Boolean)
            Float::class -> editor.putFloat(key, value as Float)
            Int::class -> editor.putInt(key, value as Int)
            Long::class -> editor.putLong(key, value as Long)
            String::class -> editor.putString(key, value as String)
            Set::class -> editor.putStringSet(key, value as Set<String>)
            // fallback to gson
            else -> {
                editor.putString(key, Gson().toJson(T::class.java))
            }
        }

        editor.apply()
    }
}