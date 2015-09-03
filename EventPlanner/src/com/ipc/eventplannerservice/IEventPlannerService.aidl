package com.ipc.eventplannerservice;

interface IEventPlannerService
{
    int sendNewEvent(String phoneNumberList, String newEventSMS);

	int sendEventReply(String phoneNumber, String eventReplySMS);
	
    int sendEventConfirmation(String phoneNumberList, String eventConfirmationSMS);
}