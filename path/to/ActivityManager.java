import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActivityManager {
    private final ClanManager clanManager;
    private final int requiredActiveMembers;

    public ActivityManager(ClanManager clanManager, int requiredActiveMembers) {
        this.clanManager = clanManager;
        this.requiredActiveMembers = requiredActiveMembers;
    }

    public List<String> getActiveClans() {
        Map<String, ClanInfo> activeClansMap = clanManager.getAllClans().stream()
            .filter(clan -> clan.getActiveMembers() >= requiredActiveMembers)
            .collect(Collectors.toMap(
                Clan::getName,
                clan -> new ClanInfo(clan.getActiveMembers(), clan.getTotalMembers())
            ));

        return activeClansMap.entrySet().stream()
            .filter(entry -> entry.getValue().getActiveMembers() >= requiredActiveMembers)
            .sorted(Comparator.<Map.Entry<String, ClanInfo>>comparingInt(e -> e.getValue().getActiveMembers())
                .thenComparingInt(e -> clanManager.getClanSize(e.getKey(), false))
                .reversed())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}
