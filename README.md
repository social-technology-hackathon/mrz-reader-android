Сканер #[Machine-readable-zone](https://en.wikipedia.org/wiki/Machine-readable_passport). Для работы необходимо:
 

 - Добавить `google-services.json` в корневую папку основного проекта.
 - Добавить `RxJava` в зависимости проекта
``` groovy
implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'  
implementation 'io.reactivex.rxjava2:rxjava:2.2.19'  
implementation 'io.reactivex.rxjava2:rxkotlin:2.4.0'
```

Пример использования:

```kotlin
 val scanner = Scanner.instance() 
 scanner.scan(it.bitmap)
		.observeOn(AndroidSchedulers.mainThread())
		.subscribe({ information ->
			Log.d("TAG", "${it.firstName} ${it.lastName})
		}, { exception -> 
			Log.e("TAG" "No MRZ zone. $exception")
		})
```
```kotlin
 val scanner = Scanner.instance() 
 scanner.findBlock(it.bitmap)
		.observeOn(AndroidSchedulers.mainThread())
		.subscribe({ mrzBitmap ->
			imageView.setImageBitmap(mrzBitmap)
		}, { exception -> 
			Log.e("TAG" "No MRZ zone. $exception")
		})
```