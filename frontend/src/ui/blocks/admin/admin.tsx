import React, {FC, memo} from 'react';
import {AddStudentToActivity} from 'src/ui/blocks/admin/add-student-to-activity';
import {ContactsActualisation} from 'src/ui/blocks/admin/contacts-actualisation';
import {NewAcademicYear} from 'src/ui/blocks/admin/new-academic-year';
import {SetStudentStatus} from 'src/ui/blocks/admin/set-student-status';
import {StudentsGroupActualisation} from 'src/ui/blocks/admin/students-group-actualisation';
import {Activity} from 'src/ui/blocks/default-form-view/activity';
import {Organisation} from 'src/ui/blocks/default-form-view/organisation';
import {Project} from 'src/ui/blocks/default-form-view/project';
import {UsersRegistration} from 'src/ui/blocks/default-form-view/users-registration';
import {HomePageParagraph} from 'src/ui/blocks/home-page-paragraph/home-page-paragraph';
import './admin.styl';

type Props = {
    selectedTab: number;
};
export const Admin: FC<Props> = memo(({selectedTab}) => {
    return (
        <div>
            <HomePageParagraph title='Панель администратора' mode={1} />
            {selectedTab === 0 && <Activity />}
            {selectedTab === 1 && <Project />}
            {selectedTab === 2 && <Organisation />}
            {selectedTab === 3 && <UsersRegistration />}
            {selectedTab === 4 && <NewAcademicYear />}
            {selectedTab === 5 && <ContactsActualisation />}
            {selectedTab === 6 && <StudentsGroupActualisation />}
            {selectedTab === 7 && <AddStudentToActivity />}
            {selectedTab === 8 && <SetStudentStatus />}
        </div>
    );
});
