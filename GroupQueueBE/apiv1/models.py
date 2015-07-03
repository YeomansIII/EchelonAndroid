from django.conf import settings
from django.db.models.signals import post_save
from django.dispatch import receiver
from rest_framework.authtoken.models import Token

from django.db import models
from django.contrib.auth.models import User

# Create your models here.
class QueueGroup(models.Model):
    #group_id = models.CharField(max_length=6)
    pin = models.CharField(max_length=6)

    FRIENDS_ONLY = 'FR'
    PIN_ONLY = 'PI'
    FRIEND_PIN_ANON = 'JR'
    FRIEND_PIN = 'SR'
    PRIVACY_CHOICES = (
        (FRIENDS_ONLY, 'Friends Only'),
        (PIN_ONLY, 'Pin Required'),
        (FRIEND_PIN_ANON, 'Pin Required For Non-Friends'),
        (FRIEND_PIN, 'Friends Only With Pin'),
    )
    privacy = models.CharField(max_length=2,
                                      choices=PRIVACY_CHOICES,
                                      default=FRIENDS_ONLY)

    def __unicode__(self):
        return self.group_id

class Listener(models.Model):
    user = models.OneToOneField(User)
    gcm_id = models.CharField(max_length=3000)
    is_leader = models.BooleanField(default=False)
    active_queuegroup = models.ForeignKey(QueueGroup, null=True, related_name="participants")
    owner_of = models.OneToOneField(QueueGroup, null=True, related_name="owner")

    def __unicode__(self):
        return self.user.username


@receiver(post_save, sender=settings.AUTH_USER_MODEL)
def create_auth_token(sender, instance=None, created=False, **kwargs):
    if created:
        Token.objects.create(user=instance)
