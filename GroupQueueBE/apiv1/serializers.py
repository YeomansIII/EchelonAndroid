from django.contrib.auth.models import User, Group
from apiv1.models import Listener, QueueGroup
from rest_framework import serializers

class UserSerializer(serializers.HyperlinkedModelSerializer):
    class Meta:
        model = User
        fields = ('url', 'pk', 'username', 'email')
        view_name = "apiv1:user-detail"


class GroupSerializer(serializers.ModelSerializer):
    class Meta:
        model = Group
        fields = ('url', 'name')
        view_name = "apiv1:group-detail"


class ListenerSerializer(serializers.ModelSerializer):
    user = UserSerializer(required=False)
    active_queuegroup = serializers.SlugRelatedField(slug_field='group_id', queryset=QueueGroup.objects, required=False)
    leader_of = serializers.SlugRelatedField(slug_field='group_id', queryset=QueueGroup.objects, required=False)

    def create(self, validated_data):
        # Create the book instance
        newuser = User.objects.create(username=validated_data['user']['username'], email=validated_data['user']['email'])
        newuser.save()

        listener = Listener.objects.create(user=newuser, gcm_id=validated_data['gcm_id'])

        return listener

    def update(self, instance, validated_data):
        # Update the book instance
        #if(validated_data['user']):
        #    instance.user.username = validated_data['user']['username']
        #    if(validated_data['user']['email']):
        #        instance.user.email = validated_data['user']['email']
        #    instance.user.save()

        if(validated_data['gcm_id']):
            instance.gcm_id = validated_data['gcm_id']
        if(validated_data['active_queuegroup']):
            instance.active_queuegroup = QueueGroup.objects.get(group_id=validated_data['active_queuegroup'])
        if(validated_data['leader_of']):
            instance.leader_of = QueueGroup.objects.get(group_id=validated_data['leader_of'])
        instance.save()

        return instance

    class Meta:
        model = Listener
        fields = ('url', 'pk','user', 'gcm_id', 'active_queuegroup', 'leader_of')
        view_name = "apiv1:listener-detail"


class QueueGroupSerializer(serializers.ModelSerializer):
    leader = ListenerSerializer(required=False)
    participants = ListenerSerializer(many=True, required=False)

    class Meta:
        model = QueueGroup
        fields = ('url', 'pk', 'group_id', 'leader', 'participants')
        view_name = "apiv1:queuegroup-detail"
