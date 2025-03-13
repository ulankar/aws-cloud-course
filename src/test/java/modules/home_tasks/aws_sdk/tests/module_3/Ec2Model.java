package modules.home_tasks.aws_sdk.tests.module_3;

@lombok.Getter
public class Ec2Model {

    String instanceId;
    String state;
    String instanceType;
    String tag;
    String publicIp;
    String platform;
    String name;
    String volumeId;

    public Ec2Model(String instanceId, String state, String instanceType, String tag, String publicIp,
                    String platform, String name, String volumeId) {
        this.instanceId = instanceId;
        this.state = state;
        this.instanceType = instanceType;
        this.tag = tag;
        this.publicIp = publicIp;
        this.platform = platform;
        this.name = name;
        this.volumeId = volumeId;
    }
}
