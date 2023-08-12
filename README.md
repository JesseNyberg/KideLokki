# KideLokki


## Description
### **KideLokki is an android application made to help reserving tickets from the kide.app site.**

- #### The application works by fetching product information from the Kide.app API and using it to create a JSON body. 
- #### The JSON body is then sent as a post request to the API to reserve the tickets.

## Features

  <img src="https://raw.githubusercontent.com/skotfrii2/skotfrii2.github.io/main/kidelokkislide.gif" width="250"/>
  
- ### **AUTH Login**: 
  - #### The app uses kide.app credentials to retrieve an access token for the reserving.
    
- ### **Countdown**: 
  - #### If the event hasn't started yet, a countdown will appear and automatically reserves when sales start.
 
- ### **Foreground service**: 
  - #### The app includes a foreground service to make sure the reserving happens, even if the user closes the app from the recent apps.


## Known Bugs

 ### **Reserve and cancel spamming**
  - #### If you spam the cancel and reserve buttons, the countdown time can be incorrect. 

---
