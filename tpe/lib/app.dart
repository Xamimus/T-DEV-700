import 'package:flutter/material.dart';
import 'package:tpe/config/router.dart';
import 'package:tpe/services/bank_service.dart';

class App extends StatelessWidget {
  App({super.key});

  static const String _title = 'Payment Terminal Home';

  BankService bankService = BankService();

  @override
  void dispose() {
    bankService.killWebSocket();
  }

  @override
  Widget build(BuildContext context) {
    bankService.init();
    return MaterialApp.router(
      title: _title,
      routerConfig: router,
      theme: ThemeData(
          scaffoldBackgroundColor: const Color(0xFF03045F),
          primarySwatch: Colors.blue,
          fontFamily: "Montserrat"),
    );
  }
}
