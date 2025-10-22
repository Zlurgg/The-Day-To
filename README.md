# The-Day-To

Journal App: Android    

Placeholder to capture notes/thoughts/evolve into a project brief  
Variation around the theme of a daily journal app with a focus on mood/wellness  

Some background research on existing apps:

- https://techcrunch.com/2023/01/13/5-best-journaling-apps-log-your-thoughts-and-experiences  
- https://www.emizentech.com/blog/diary-journal-app-development.html  
- https://www.makeuseof.com/best-micro-journaling-apps-write-a-diary  
- https://friday.app/p/best-digital-journal-apps  

Development Practice/Ideas/Approach  
Aim to publish to Github as a release - using pages

General approach, built using small/testable ‘modules/components’  

## Features: 
- Jetpack Compose  
- Kotlin  
- MVVM  
- DI Koin
- Retrofit  
- Room  
- Offline Capability
- Work Manager (notifications - daily prompt)
- Google Sign in (deprecated)
- Modules  ( data -> domain <- presentation principle)

## Release
Internal testing on play store (20 testers)

## Screenshots showing use

### Daily Entry Screen  
Calendar (side scroll between months) days given colors by user created moods, daily entries listed below calendar.  
Navigation by clicking on either calendar day or entry in list.  

[![Screenshot-2024-12-02-15-50-04-64-9a482b403f0c7505d2f7b6ec2803966a.jpg](https://i.postimg.cc/hvh5t3hJ/Screenshot-2024-12-02-15-50-04-64-9a482b403f0c7505d2f7b6ec2803966a.jpg)](https://postimg.cc/8fQHy4RD)  

[![Screenshot-2024-12-02-15-50-18-85-9a482b403f0c7505d2f7b6ec2803966a.jpg](https://i.postimg.cc/hj2Ygq4W/Screenshot-2024-12-02-15-50-18-85-9a482b403f0c7505d2f7b6ec2803966a.jpg)](https://postimg.cc/8Jr4B9rt)  

### Mood Color Screen  
Following daily notification create a new entry for current day with a mood (with color) and a optional note - (can also create a new mood color relation).  

[![Screenshot-2024-12-02-15-49-19-53-9a482b403f0c7505d2f7b6ec2803966a.jpg](https://i.postimg.cc/TPqS7DZP/Screenshot-2024-12-02-15-49-19-53-9a482b403f0c7505d2f7b6ec2803966a.jpg)](https://postimg.cc/zLv03VDm)  

[![Screenshot-2024-12-02-15-49-51-59-9a482b403f0c7505d2f7b6ec2803966a.jpg](https://i.postimg.cc/pTWSgdHJ/Screenshot-2024-12-02-15-49-51-59-9a482b403f0c7505d2f7b6ec2803966a.jpg)](https://postimg.cc/WDySZTtd)  

[![Screenshot-2024-12-02-16-05-41-20-9a482b403f0c7505d2f7b6ec2803966a.jpg](https://i.postimg.cc/h43Y5g3V/Screenshot-2024-12-02-16-05-41-20-9a482b403f0c7505d2f7b6ec2803966a.jpg)](https://postimg.cc/K4BQKXbY)  

### Google Sign In  
Basic google sign in set up  

[![Screenshot-2024-12-02-15-50-43-76-f7aa348215f5d566f9e4ca860f474209.jpg](https://i.postimg.cc/x87FJyb6/Screenshot-2024-12-02-15-50-43-76-f7aa348215f5d566f9e4ca860f474209.jpg)](https://postimg.cc/tn3z86k6)  

### Notification
Daily notification

[![Screenshot-2024-12-02-164416.png](https://i.postimg.cc/qMsxFbyX/Screenshot-2024-12-02-164416.png)](https://postimg.cc/0bNSMCtb)  
