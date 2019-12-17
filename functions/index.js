const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
const db = functions.region('europe-west1').firestore;

// -- End of initialization --

/**
 * *Listens to new messages*.
 * - Increases a counter that represents the amount of messages in this chat room.
 * - Updates the timestamp of the last received message in this chat room.
 * @type {CloudFunction<DocumentSnapshot>}
 */
exports.onMessageSent = db.document('chats/{chatId}/messages/{messageId}').onCreate((snap, context) => {
    const chatDocRef = admin.firestore()
        .collection('chats')
        .doc(context.params.chatId);

    return admin.firestore().runTransaction(function (transaction) {
        return transaction.get(chatDocRef).then(function (chatDoc) {
            if (!chatDoc.exists) {
                throw new "Chat room does not exist";
            }
            transaction.update(chatDocRef, {messagesCount: chatDoc.data().messagesCount + 1});
            transaction.update(chatDocRef, {timestamp: snap.data().timestamp});
            return null;
        });
    }).then(function () {
        console.log("Updated messages counter");
        return null;
    }).catch(function (error) {
        console.log("Error updating messages counter: ", error);
        return null;
    });

});


/**
 * *Listens to new messages.*
 * - Sends a notification to the receiving end of a chat message
 *
 * @type {CloudFunction<DocumentSnapshot>}
 */
exports.onMessageSentNotify = db.document('chats/{chatId}/messages/{messageId}').onCreate((snap, context) => {
    return admin.firestore()
        .collection('students')
        .doc(snap.data().receiver)
        .get()
        .then(receiverDoc => {
            return admin.firestore()
                .collection('students')
                .doc(snap.data().sender)
                .get()
                .then(senderDoc => {
                    return admin.firestore()
                        .collection('chats')
                        .doc(context.params.chatId)
                        .get()
                        .then(chatDoc => {
                            console.log('Creating a chat notification for: ', receiverDoc.data().name);
                            let tokens = receiverDoc.data().tokens;
                            let photo = "none";
                            if ('photo' in senderDoc.data() &&
                                senderDoc.data().photo !== 'undefined' &&
                                senderDoc.data().photo !== null) {
                                photo = senderDoc.data().photo;
                            }
                            const payload = {

                                data: {
                                    display_status: "admin_broadcast",
                                    notification_type: "CHAT",
                                    title: senderDoc.data().name,
                                    body: snap.data().text,
                                    sender_id: senderDoc.data().uid,
                                    sender_photo_url: photo,
                                    chat_id: chatDoc.data().chat,
                                }

                            };

                            console.log("Sending notification");
                            return admin.messaging().sendToDevice(tokens, payload)
                                .then(function (response) {
                                    console.log("Successfully sent chat notification\nResponse: ", response);
                                    return response;
                                })
                                .catch(function (error) {
                                    console.log("Error sending chat notification\nError message: ", error);
                                });
                        });
                });
        });
});