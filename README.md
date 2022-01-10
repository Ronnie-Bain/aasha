# Aasha
Aasha is an android chat app where authenticated users can chat and send images globally. The user authentication is done using **Firebase** by **Google** and **Facebook** signin providers.

<img src="https://user-images.githubusercontent.com/73740367/148738287-e80bd252-507d-4f79-9570-5a3f54cacac1.jpg" width="190" height="340" />

It has a single chamber chat section where connected users exchange texts and images. An object which contains the message time, date and content is stored and retrived from a Firebase realtime database everytime a user pushes a message. 
Any insertion or changes to the database are shown to the screen by a `RecyclerView` and an adapter that extends `FirebaseRecylerAdapter`. The messages are structured by their dates.

<img src="https://user-images.githubusercontent.com/73740367/148742101-fe0a032c-033b-4a3d-aa2e-2d53ecb90bea.jpg" width="190" height="340" />

The main activity deployes CoordinatorLayout as it's parent layout listening recylerview scrolls and hiding the toolbar on upward scroll. The user list is shown on a different activity. Users can also edit names or update their profile pictures.

<img src="https://user-images.githubusercontent.com/73740367/148742146-002ff9b0-2b37-412e-ab6b-77e5a6ea980f.jpg" width="190" height="340" /> <img src="https://user-images.githubusercontent.com/73740367/148742155-641321b6-4c0b-4e89-aafa-e43f4faafadf.jpg" width="190" height="340" /> <img src="https://user-images.githubusercontent.com/73740367/148742162-f29bd3ca-7a7a-47e0-b57e-127b9c46131c.jpg" width="190" height="340" />
