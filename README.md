# Rate Your Peer (PeerRate)

This code runs the PeerRate platform

To run this code, simply run the following commands in the root directory:
1. mvn clean package -DskipTests
2. docker-compose build
3. docker-compose up
4. This will take 5-10 minutes, and will cause many exceptions in the process as the services
   go up - some services depend on the others to go up. Once the logs stop eventually, go to
   http://localhost:8000/accounts/signup. You should now be able to use the website like in 
   the video.

Link to report: https://gitlab.com/comp30220/2022/rate-your-peer/-/blob/main/PeerRate%20Report.pdf

Link to video: