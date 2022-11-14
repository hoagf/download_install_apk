import 'dart:async';

import 'package:flutter/services.dart';

class DownloadInstallApk {
  static const EventChannel _progressChannel =
      EventChannel('com.zhgwu.download.install.apk');
  static const EventChannel _installChanel =
      EventChannel('com.zhgwu.install.apk');

  Stream<Event>? _progressStream;

  Stream<Event> execute(String url) {
    final StreamController<Event> controller =
        StreamController<Event>.broadcast();
    if (_progressStream == null) {
      _progressChannel.receiveBroadcastStream(
        <dynamic, dynamic>{
          'url': url,
        },
      ).listen((dynamic event) {
        final Event otaEvent = _toEvent(event.cast<String>());
        controller.add(otaEvent);
      }).onError((Object error) {
        if (error is PlatformException) {
          controller.add(_toEvent(<String?>[error.code, error.message]));
        }
      });
      _progressStream = controller.stream;
    }
    return _progressStream!;
  }

  Stream<Event> install(String filePath) {
    final StreamController<Event> controller =
        StreamController<Event>.broadcast();
    _installChanel.receiveBroadcastStream(
      <dynamic, dynamic>{
        'path': filePath,
      },
    ).listen((dynamic event) {
      final Event otaEvent = _toEvent(event.cast<String>());
      controller.add(otaEvent);
    }).onError((Object error) {
      if (error is PlatformException) {
        controller.add(_toEvent(<String?>[error.code, error.message]));
      }
    });
    return controller.stream;
  }

  Event _toEvent(List<String?> event) {
    return Event(Status.values[int.parse(event[0]!)], event[1]);
  }
}

class Event {
  Event(this.status, this.value);
  Status status;
  String? value;

  @override
  String toString() {
    return 'Event{status: $status, value: $value}';
  }
}

enum Status {
  downloading,
  downloadError,
  installing,
  installError,
  alreadyRunningError,
}
