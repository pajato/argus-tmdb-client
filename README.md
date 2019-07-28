# argus-tmdb-client

![TMDB PoweredBy Logo](https://www.themoviedb.org/assets/2/v4/logos/408x161-powered-by-rectangle-green-bb4301c10ddc749b4e79463811a68afebeae66ef43d17bcfd8ff0e60ded7ce99.png)

![license: LGPL v3](https://img.shields.io/badge/license-LGPL%20v3-blue.svg)
![build: passing](https://img.shields.io/badge/build-passing-brightgreen.svg)
![codecov: 100](https://img.shields.io/badge/codecov-100%25-brightgreen.svg)

## Overview

A multi-platform Kotlin library providing dataset access to The Movie Database (tmdb) for Android and iOS devices.

## API

```kotlin

const val BASE_URL = "https://tmdb.pajato.com"

/** Access the reference (first) page returning a TmdbData object of type T1 or TmdbError.*/
fun <T1: TmdbData> getFirstPage(type: KClass<T1>, pageSize: Int, baseUrl: String = BASE_URL): Page<TmdbData>

/** Access the next page returning a TmdbData object of type T1 or TmdbError.*/
fun <T1: TmdbData> getNextPage(type: KClass<T1>, pageSize: Int): Page<TmdbData>

/** Access the previous page returning a TmdbData object of type T1 or TmdbError.*/
fun <T1: TmdbData> getPrevPage(type: KClass<T1>, pageSize: Int, page: Page<T1>): Page<TmdbData>

```
Version 0.1.0 is available via jcenter or Maven Central using: **"com.pajato.argus:argus-tmdb-client:0.1.0"**
