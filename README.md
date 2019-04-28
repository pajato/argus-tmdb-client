# argus-tmdb-client

![TMDB PoweredBy Logo](images/powered-by-tmdb.png)

## Overview

A multi-platform Kotlin library providing access to The Movie Database (tmdb) for Android and iOS devices.

## API

```kotlin
/** Access the reference (first) page returning a TmdbData object of type T1 or TmdbError.*/
fun <T1: TmdbData, T2: TmdbData> getPage(type: KClass<T1>, pageSize: Int): Page<T2>

/** Access the next page returning a TmdbData object of type T1 or TmdbError.*/
fun <T1: TmdbData, T2: TmdbData> getNextPage(type: KClass<T1>, pageSize: Int, Page<T1>): Page<T2>

/** Access the previous page returning a TmdbData object of type T1 or TmdbError.*/
fun <T1: TmdbData, T2: TmdbData> getPrevPage(type: KClass<T1>, pageSize: Int, Page<T1>): Page<T2>

```
