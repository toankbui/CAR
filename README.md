# CAR | Pre-release
Catch A Ride | A long-distance ride-share solution for UCSD students.
Created by team CAR in 2018

Introduction: 
Owning a car while attending UC San Diego is a luxury that many students are unable to enjoy. However, one thing that all students do enjoy is having the ability to visit home or some places outside of La Jolla that have significance to them. The only issue is that without a car, the cost of doing so is quite unappealing and often deters students from going where they want to go. Catch a Ride (CAR) is a phone application designed by students, for students, with the motivation to simplify commuting to and from UC San Diego, all at a significantly cheaper cost compared to Uber, public transit, and plane rides. CAR’s vision is to allow students that do own a car to have the ability to assist those that do not, in a mutually beneficial way that promotes a sense of community within the university.


Login Credentials: 

Pre-populated accounts:

   - Account 1 (Driver): tkb001@ucsd.edu   	  Password: 123456 
   
   - Account 2 (Rider):  cdouaihy@ucsd.edu    Password: firebase
      
Unpopulated account: To test registration/verification,etc..., please use your own UCSD email to register for an account.       
     
     
Requirements: 
   - Two Android devices running on Android KitKat 4.4 and above.
   - Minimum APK 19.
   - UCSD emails (credentials provided above).
   - Stable internet connection.
   

Installation Instruction: 

   Way 1: Using Android devices:
   
      1. Navigate to https://github.com/joshydotpoo/CAR/releases from the phone
      
      2. Download app-debug.apk under CAR PRE-RELEASE (v1.0.1)
      
      3. Launch the app

   Way 2: Using Android studio emulator:
      
      1. Navigate to https://github.com/joshydotpoo/CAR from a computer with Android studio installed
      
      2. Clone the repository
      
      3. Open the project using Android Studio and hit Run
     
     
How to Run: 
   - After downloading app-debug.apk on the Android devices, look for the CAR application and launch it.
   

Known Bugs: 
   Splash screen loop: the app may get stuck on the splash screen if the wifi/cellular connection is unstable. 
      - Work-around: connect to a stable wifi/cellular connection and restart the app.

***************************************************************************************************************************
## Git commands:
1. Use `git clone https://github.com/joshydotpoo/CAR.git` to copy the entire repo.
2. Use `cd CAR` to go inside the repo!
3. Use `git branch <branch name>` to create a new branch.
4. Use `git checkout <branch name>` to switch to another branch.
   - Use `git pull` first if checking out a branch from GitHub to make sure you get the most up to date branch.
5. Use `git add <file name>` to add a file to commit.
   - Use `git add .` to add all changed files in current directories and below to commit.
5. Use `git commit -m "<commit message>"` to commit changes.
7. Use `git push` to push changes to current branch.
   - Use `git push origin master` if first time pushing from this branch.
8. Use the following to merge two branches: 
   - Use `git checkout <branch A>` to switch to branch you want to merge upstream
   - Use `git merge <branch B>` to merge branch B into branch A.
   - Use `git commit -a` to commit your changes.
   - Use `git push origin master` to push these changes to GitHub.
9. Use `git pull` to update branch with what is on GitHub.


