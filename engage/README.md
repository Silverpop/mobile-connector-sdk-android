EngageSDK-Android
=================

Silverpop Engage SDK for Android (a.k.a. the "Silverpop Mobile Connector")

## EngageConfg

EngageConfig provides the mechanism for managing Engage configurations in the Android realm. EngageConfig
contains static methods that when presented with your applications Context class will manage
configuration values for you.

## XMLAPI

Java object to represent XMLAPI requests in a more objective manner. The Object can can be created
and manipulated to the user preference and then serialized to the XML payload via the XMLAPI.envelope()
method on the object.

## UBF
User Behavior Functions.

|UBF Event Name   |UBF Event Type |
|-----------------|---------------|
INSTALLED|12
SESSION STARTED|13
SESSION ENDED|14
GOAL ABANDONED|15
GOAL COMPLETED|16
NAMED EVENT|17
RECEIVED NOTIFICATION|48
OPENED NOTIFICATION|49


### UBF Payload
All UBF events will contain the following fields.

* Device Name
* Device Version
* OS Name
* OS Version
* App Name
* App Version
* Device Id
* Primary User Id
* Anonymous Id

## NOTES

UBF session expired and session restart must be explicity invoked by the SDK user at the start of the application since android cannot listen for Android app startup messages from the SDK level.

SDK user must setup an activity with an intent-filter to send opened URL message to the appropriate Silverpop SDK handler.