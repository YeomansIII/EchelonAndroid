from django.conf import settings
from django.db.models.signals import post_save
from django.dispatch import receiver
from rest_framework.authtoken.models import Token

from django.db import models
from django.contrib.auth.models import User

# Create your models here.
class QueueGroup(models.Model):
    group_id = models.CharField(max_length=6)

    def __unicode__(self):
        return self.group_id

class Listener(models.Model):
    user = models.OneToOneField(User)
    gcm_id = models.CharField(max_length=3000)
    active_queuegroup = models.ForeignKey(QueueGroup, null=True, related_name="participants")
    leader_of = models.OneToOneField(QueueGroup, null=True, related_name="leader")

    def __unicode__(self):
        return self.user.username


@receiver(post_save, sender=settings.AUTH_USER_MODEL)
def create_auth_token(sender, instance=None, created=False, **kwargs):
    if created:
        Token.objects.create(user=instance)
