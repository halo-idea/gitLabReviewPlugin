package cn.kuwo.intellij.plugin;

import cn.kuwo.intellij.plugin.actions.StatusPopupAction;
import cn.kuwo.intellij.plugin.bean.FilterBean;
import cn.kuwo.intellij.plugin.bean.GitlabMergeRequestWrap;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class RMListObservable extends Observable {
    private static RMListObservable instance;
    private ArrayList<GitlabMergeRequestWrap> gitlabMergeRequests;

    private RMListObservable() {

    }

    public static RMListObservable getInstance() {
        if (instance == null) {
            instance = new RMListObservable();
        }
        return instance;
    }

    public void refreshList() {
        List<GitlabMergeRequestWrap> result = new ArrayList<>();
        if (gitlabMergeRequests != null) {
            for (GitlabMergeRequestWrap gitlabMergeRequest : gitlabMergeRequests) {
                String reviewer = FilterBean.getInstance().getReviewer();
                if (reviewer != null && !reviewer.trim().isEmpty() && !gitlabMergeRequest.gitlabMergeRequest.getAssignee().getName().contains(reviewer)) {
                    continue;
                }
                String owner = FilterBean.getInstance().getOwner();
                if (owner != null && !owner.trim().isEmpty() && !gitlabMergeRequest.gitlabMergeRequest.getAuthor().getName().contains(owner)) {
                    continue;
                }
                String fromBranch = FilterBean.getInstance().getFromBranch();
                if (fromBranch != null && !fromBranch.trim().isEmpty() && !gitlabMergeRequest.gitlabMergeRequest.getSourceBranch().contains(fromBranch)) {
                    continue;
                }
                String toBranch = FilterBean.getInstance().getToBranch();
                if (toBranch != null && !toBranch.trim().isEmpty() && !gitlabMergeRequest.gitlabMergeRequest.getTargetBranch().contains(toBranch)) {
                    continue;
                }
                String status = FilterBean.getInstance().getStatus();
                if (status != null && !status.trim().isEmpty() && !gitlabMergeRequest.gitlabMergeRequest.getState().contains(status)) {
                    continue;
                }
                String searchKey = FilterBean.getInstance().getSearchKey();
                if (searchKey != null) {
                    boolean titleContain = gitlabMergeRequest.gitlabMergeRequest.getTitle() != null && gitlabMergeRequest.gitlabMergeRequest.getTitle().toLowerCase().contains(searchKey.trim().toLowerCase());
                    boolean discriptionContain = gitlabMergeRequest.gitlabMergeRequest.getDescription() != null && gitlabMergeRequest.gitlabMergeRequest.getDescription().toLowerCase().contains(searchKey.trim().toLowerCase());
                    if (!searchKey.trim().isEmpty() && !titleContain && !discriptionContain) {
                        continue;
                    }
                }
                result.add(gitlabMergeRequest);
            }
        }
        setChanged();
        notifyObservers(result);
    }

    public void filterSearchKey(String key) {
        FilterBean.getInstance().setSearchKey(key);
        refreshList();
    }

    public void filterOwner(String owner) {
        FilterBean.getInstance().setOwner(owner);
        refreshList();
    }

    public void filterReviewer(String reviewer) {
        FilterBean.getInstance().setReviewer(reviewer);
        refreshList();
    }

    public void filterStatus(StatusPopupAction.Status status) {
        FilterBean.getInstance().setStatus(status == StatusPopupAction.Status.All ? "" : status.name());
        refreshList();
    }

    public void filterToBranch(String branch) {
        FilterBean.getInstance().setToBranch(branch);
        refreshList();
    }

    public void filterFromBranch(String branch) {
        FilterBean.getInstance().setFromBranch(branch);
        refreshList();
    }

    public void resetList(ArrayList<GitlabMergeRequestWrap> gitlabMergeRequests) {
        this.gitlabMergeRequests = gitlabMergeRequests;
        refreshList();
    }
}
