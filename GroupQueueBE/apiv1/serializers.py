from django.contrib.auth.models import User, Group
from apiv1.models import Listener, QueueGroup, QueueTrack
from rest_framework import serializers

class UserSerializer(serializers.HyperlinkedModelSerializer):
    class Meta:
        model = User
        fields = ('url', 'pk', 'username', 'password', 'email')
        extra_kwargs = {'password': {'write_only': True},'email':{'write_only':True}}
        view_name = "apiv1:user-detail"


class GroupSerializer(serializers.ModelSerializer):
    class Meta:
        model = Group
        fields = ('url', 'name')
        view_name = "apiv1:group-detail"


class QueueTrackSeializer(serializers.ModelSerializer):
    class Meta:
        model = QueueTrack
        fields = ('pk', 'spotify_id','rating', 'played')
        view_name = "apiv1:queuetrack-detail"


class ListenerSerializer(serializers.HyperlinkedModelSerializer):
    user = UserSerializer(required=False)
    active_queuegroup = serializers.PrimaryKeyRelatedField(queryset=QueueGroup.objects, required=False, allow_null=True)
    owner_of = serializers.HyperlinkedIdentityField(read_only=True, view_name="apiv1:queuegroup-detail")
    gcm_id = serializers.CharField(required=False, write_only=True)

    def create(self, validated_data):
        # Create the book instance
        newuser = User.objects.create(username=validated_data['user']['username'], email=validated_data['user']['email'])
        newuser.set_password(validated_data['user']['password'])
        newuser.save()

        listener = Listener.objects.create(user=newuser)

        if 'gcm_id' in validated_data:
            setattr(listener, 'gcm_id', validated_data['gcm_id'])

        queuegroup = QueueGroup.objects.create()
        listener.owner_of = queuegroup

        listener.save()

        return listener

    def update(self, instance, validated_data):
        # Update the book instance
        if 'user' in validated_data:
            instance.user.username = validated_data['user']['username']
            if 'email' in validated_data['user']:
                instance.user.email = validated_data['user']['email']
            instance.user.save()

        if 'gcm_id' in validated_data:
            instance.gcm_id = validated_data['gcm_id']
        if 'is_leader' in validated_data:
            instance.is_leader = validated_data['is_leader']
        if 'active_queuegroup' in validated_data:
            instance.active_queuegroup = validated_data['active_queuegroup']
        if 'owner_of' in validated_data:
            instance.owner_of = validated_data['owner_of']
        instance.save()

        return instance

    class Meta:
        model = Listener
        fields = ('url', 'pk','user', 'gcm_id', 'owner_of', 'is_leader', 'active_queuegroup')
        view_name = "apiv1:listener-detail"


class QueueGroupSerializer(serializers.HyperlinkedModelSerializer):
    owner = ListenerSerializer(read_only=True,required=False)
    participants = ListenerSerializer(many=True,read_only=True, required=False)
    track_queue = QueueTrackSeializer(many=True, read_only=True)

    class Meta:
        model = QueueGroup
        fields = ('url', 'pk', 'is_active',  'owner', 'participants', 'track_queue')
        extra_kwargs = {'is_active':{'read_only':True}}
        view_name = "apiv1:queuegroup-detail"
