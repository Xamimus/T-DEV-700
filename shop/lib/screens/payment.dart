import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:shop/screens/shop.dart';
import 'package:shop/widgets/snackBar.dart';
import 'package:shop/connectors/requests.dart';

import '../util/shop.dart';
import '../widgets/separation.dart';

// Class representing a payment page
class Payment extends StatelessWidget {
  // Static constant for the route name of the page
  static const String pageName = '/payment';
  // Variable for the total price of the articles in the shop
  String? total = totalPrice();
  // Static late BuildContext variable for the context of the payment page
  static late BuildContext contextPayment;

  final RequestsClass requestsClass = RequestsClass();

  // Constructor for the Payment class that takes in a required key
  Payment({super.key});

  // Method that builds and returns the widget tree for the Payment widget
  @override
  Widget build(BuildContext context) {
    contextPayment = context;
    return Scaffold(
      body: Container(
        color: Colors.white,
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              Expanded(
                  child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: <Widget>[
                  const Text(
                    'Paiement en cours ...',
                    style: TextStyle(
                      color: Colors.black,
                      fontSize: 30,
                    ),
                  ),
                  const SizedBox(height: 20),
                  const Image(
                    image: AssetImage('images/man_wait.gif'),
                    width: 200,
                    height: 200,
                  ),
                  TextButton(
                    onPressed: () {
                      requestsClass.cancelPayment();
                    },
                    style: ButtonStyle(
                      backgroundColor: MaterialStateProperty.all<Color>(
                        const Color.fromARGB(255, 255, 142, 13),
                      ),
                    ),
                    child: const Text(
                      'Annuler le paiement',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 20,
                      ),
                    ),
                  ),
                ],
              )),
              Column(
                mainAxisAlignment: MainAxisAlignment.end,
                children: <Widget>[
                  const Separation(),
                  const SizedBox(height: 10),
                  Text(
                    '$total €',
                    style: const TextStyle(
                      color: Colors.black,
                      fontSize: 50,
                    ),
                  ),
                  const SizedBox(height: 20),
                ],
              )
            ],
          ),
        ),
      ),
    );
  }
}
