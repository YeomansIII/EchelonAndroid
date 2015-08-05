# Echelon (aka GroupQueue) - DEV BRANCH
A Spotify jukebox created for small to large groups of individuals to listen to music together from one device.

A leader creates a group and allows other users to join that group and queue up music of their own liking.  All users in the group can then up and down vote songs that they do and don't like, in return affecting the order of the queue.

Echelon is good for parties, listening to music in the car, and public use at restaurants and other venues (though this is something that would need to be discussed with Spotify, probably can't do this.)

## Technical Specs
**Android min SDK:** 16

**Android target SDK:** 21

**Libraries:**
  - Spotify-Auth == 1.0.0-beta10
  - Spotify-Player == 1.0.0-beta10
  - Crashlytics == 2.4.0
  - Android appcompat-v7 == 22.2.0
  - Android support-v4 == 22.2.0
  - Android design == 22.2.0
  - Google play-services-gcm == 7.5.0

**Continuos Integration:**
This project builds to: https://jenkins.yeomans.io/job/dev-echelon/
