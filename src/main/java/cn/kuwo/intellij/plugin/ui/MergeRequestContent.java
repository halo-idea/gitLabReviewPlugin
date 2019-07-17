package cn.kuwo.intellij.plugin.ui;

import cn.kuwo.intellij.plugin.RMListObservable;
import cn.kuwo.intellij.plugin.actions.*;
import cn.kuwo.intellij.plugin.bean.Branch;
import cn.kuwo.intellij.plugin.ui.BaseMergeRequestCell.BaseMergeRequestCell;
import cn.kuwo.intellij.plugin.ui.MergeRequestDetail.MergeRequestDetail;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.changes.ui.ChangesViewContentProvider;
import com.intellij.openapi.vcs.ui.SearchFieldAction;
import com.intellij.ui.JBSplitter;
import com.intellij.util.NotNullFunction;
import git4idea.GitVcs;
import org.gitlab.api.models.GitlabBranch;
import org.gitlab.api.models.GitlabMergeRequest;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class MergeRequestContent implements ChangesViewContentProvider {

    private Project project;
    private JList requestList;
    private JPanel panel1;
    private DataModel dataModel;
    private Observer requestListObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            if (requestList != null) {
                if (arg != null && arg instanceof List) {
                    dataModel.arrayList = (List<GitlabMergeRequest>) arg;
                } else {
                    dataModel.arrayList = null;
                }
                requestList.updateUI();
            }
        }
    };
    private JBSplitter horizontalSplitter;

    public MergeRequestContent(Project project) {
        this.project = project;
    }

    @Override
    public JComponent initContent() {
        horizontalSplitter = new JBSplitter(false, 0.7f);
        SimpleToolWindowPanel basePan = new SimpleToolWindowPanel(true, true);
        ActionToolbar actionToolbar = getToolBar(project);
        actionToolbar.setTargetComponent(requestList);
        basePan.setToolbar(actionToolbar.getComponent());
        basePan.setContent(panel1);
        horizontalSplitter.setFirstComponent(basePan);
        dataModel = new DataModel();
        requestList.setModel(dataModel);
        requestList.setCellRenderer(new MRCommentCellRender());
        requestList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    GitlabMergeRequest gitlabMergeRequest = dataModel.getElementAt(requestList.getSelectedIndex());
                    Branch srcBranch = new Branch();
                    srcBranch.repoName = "origin";
                    srcBranch.gitlabBranch = new GitlabBranch();
                    srcBranch.gitlabBranch.setName(gitlabMergeRequest.getSourceBranch());
                    Branch targetBranch = new Branch();
                    targetBranch.repoName = "origin";
                    targetBranch.gitlabBranch = new GitlabBranch();
                    targetBranch.gitlabBranch.setName(gitlabMergeRequest.getTargetBranch());
                    MergeRequestDetail mergeRequestDetail = MergeRequestDetail.getMergeRequestDetail(project,gitlabMergeRequest);
                    horizontalSplitter.setSecondComponent(mergeRequestDetail.getBasePan());
                }
            }
        });
        RMListObservable.getInstance().addObserver(requestListObserver);
        return horizontalSplitter;
    }

    @Override
    public void disposeContent() {
        RMListObservable.getInstance().deleteObserver(requestListObserver);
    }

    private ActionToolbar getToolBar(Project project) {
        DefaultActionGroup toolBarActionGroup = (DefaultActionGroup) ActionManager.getInstance().getAction("GitMergeRequest.Toolbar");
        SearchFieldAction searchFieldAction = new SearchFieldAction("") {
            @Override
            public void actionPerformed(AnActionEvent event) {
                RMListObservable.getInstance().filterSearchKey(getText().trim());
            }
        };
        toolBarActionGroup.add(searchFieldAction);
//        状态
        StatusPopupAction branchPopupAction = new StatusPopupAction(project, "Status");
        toolBarActionGroup.add(branchPopupAction);
//        原始分支
        FromBranchPopupAction fromBranchPopupAction = new FromBranchPopupAction(project, "FromBranch");
        toolBarActionGroup.add(fromBranchPopupAction);
//        目标分支
        ToBranchPopupAction toBranchPopupAction = new ToBranchPopupAction(project, "ToBranch");
        toolBarActionGroup.add(toBranchPopupAction);
//        检查者
        ReviewerPopupAction reviewerPopupAction = new ReviewerPopupAction(project, "Reviewer");
        toolBarActionGroup.add(reviewerPopupAction);
//        发起者
        OwnerPopupAction ownerPopupAction = new OwnerPopupAction(project, "Owner");
        toolBarActionGroup.add(ownerPopupAction);
        //分割线
        toolBarActionGroup.addSeparator();
//        刷新
        RefreshAction refreshAction = new RefreshAction(project);
        toolBarActionGroup.add(refreshAction);
        return ActionManager.getInstance().createActionToolbar("GitMergeRequest.Toolbar", toolBarActionGroup, true);
    }


    public static class VisibilityPredicate implements NotNullFunction<Project, Boolean> {
        @NotNull
        @Override
        public Boolean fun(@NotNull Project project) {
            return ProjectLevelVcsManager.getInstance(project).checkVcsIsActive(GitVcs.NAME);
        }
    }

    public class DataModel extends AbstractListModel {
        private List<GitlabMergeRequest> arrayList;

        @Override
        public GitlabMergeRequest getElementAt(int index) {
            if (arrayList == null) {
                return null;
            }
            return arrayList.get(index);
        }

        @Override
        public int getSize() {
            return arrayList == null ? 0 : arrayList.size();
        }
    }

    public class MRCommentCellRender extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            GitlabMergeRequest mergeRequest = value instanceof GitlabMergeRequest ? ((GitlabMergeRequest) value) : null;
            if (mergeRequest != null) {
                BaseMergeRequestCell mergeRequestCell = BaseMergeRequestCell.getMergeRequestCell(mergeRequest);
                if (requestList.getSelectedIndex() == index) {
                    mergeRequestCell.setBackGround(0xff4B6EAF);
                } else {
                    mergeRequestCell.setBackGround(0xff3C3F41);
                }
                return mergeRequestCell.getBasePan();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

}
